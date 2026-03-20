import { HttpStatus, Injectable, OnModuleDestroy } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Redis from 'ioredis';
import { randomUUID } from 'crypto';
import { BusinessException } from '../common/exceptions/business.exception';

export interface DistributedLock {
  key: string;
  token: string;
}

export interface AcquireLockOptions {
  ttlMs?: number;
  maxRetries?: number;
  retryDelayMs?: number;
}

@Injectable()
export class RedlockService implements OnModuleDestroy {
  private readonly redis: Redis;

  constructor(private readonly configService: ConfigService) {
    this.redis = new Redis({
      host: this.configService.get<string>('REDIS_HOST', '127.0.0.1'),
      port: this.configService.get<number>('REDIS_PORT', 6379),
      password: this.configService.get<string>('REDIS_PASSWORD') || undefined,
      lazyConnect: true,
      maxRetriesPerRequest: 1,
      connectTimeout: 1000,
      commandTimeout: 1500,
      enableOfflineQueue: false,
    });
  }

  async acquireLocks(keys: string[], options?: AcquireLockOptions): Promise<DistributedLock[]> {
    const uniqueKeys = [...new Set(keys)].sort();
    if (!uniqueKeys.length) {
      return [];
    }

    const ttlMs = options?.ttlMs ?? 5000;
    const maxRetries = options?.maxRetries ?? 5;
    const retryDelayMs = options?.retryDelayMs ?? 120;
    const token = randomUUID();

    await this.ensureConnection();

    for (let attempt = 1; attempt <= maxRetries; attempt += 1) {
      const acquired: DistributedLock[] = [];
      try {
        for (const key of uniqueKeys) {
          const result = await this.redis.set(key, token, 'PX', ttlMs, 'NX');
          if (result !== 'OK') {
            throw new Error('LOCK_NOT_ACQUIRED');
          }
          acquired.push({ key, token });
        }
        return acquired;
      } catch (error) {
        await this.releaseLocks(acquired);
        if (attempt === maxRetries) {
          throw new BusinessException(
            'VALIDATION_FAILED',
            HttpStatus.CONFLICT,
            '동시 주문이 많아 잠시 후 다시 시도해주세요.',
          );
        }
        await this.sleep(retryDelayMs);
      }
    }

    throw new BusinessException('VALIDATION_FAILED', HttpStatus.CONFLICT);
  }

  async releaseLocks(locks: DistributedLock[]) {
    if (!locks.length) {
      return;
    }

    await this.ensureConnection();

    const releaseScript = `
      if redis.call("get", KEYS[1]) == ARGV[1] then
        return redis.call("del", KEYS[1])
      else
        return 0
      end
    `;

    for (const lock of locks) {
      try {
        await this.redis.eval(releaseScript, 1, lock.key, lock.token);
      } catch {
        // 락 해제 실패는 다음 요청에서 TTL로 정리되므로 여기서는 무시한다.
      }
    }
  }

  async onModuleDestroy() {
    if (this.redis.status !== 'end') {
      await this.redis.quit();
    }
  }

  private async ensureConnection() {
    if (this.redis.status === 'wait') {
      await this.redis.connect();
    }
  }

  private async sleep(ms: number) {
    await new Promise((resolve) => setTimeout(resolve, ms));
  }
}
