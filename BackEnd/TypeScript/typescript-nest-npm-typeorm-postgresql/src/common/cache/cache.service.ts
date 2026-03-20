import { Injectable, OnModuleDestroy } from '@nestjs/common';
import Redis from 'ioredis';

@Injectable()
export class CacheService implements OnModuleDestroy {
  private readonly redis: Redis;

  constructor() {
    this.redis = new Redis({
      host: process.env.REDIS_HOST ?? '127.0.0.1',
      port: Number(process.env.REDIS_PORT ?? 6379),
      password: process.env.REDIS_PASSWORD || undefined,
      lazyConnect: true,
      maxRetriesPerRequest: 1,
      connectTimeout: 1000,
      commandTimeout: 1500,
      enableOfflineQueue: false,
    });
  }

  async getJson<T>(key: string): Promise<T | null> {
    await this.ensureConnection();
    const raw = await this.redis.get(key);
    if (!raw) return null;

    try {
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }

  async setJson(key: string, value: unknown, ttlSeconds: number) {
    await this.ensureConnection();
    await this.redis.set(key, JSON.stringify(value), 'EX', ttlSeconds);
  }

  async del(...keys: string[]) {
    if (!keys.length) return;
    await this.ensureConnection();
    await this.redis.del(...keys);
  }

  // 패턴 기반 무효화는 운영 중 다수 키 삭제가 필요할 때 사용한다.
  async delByPattern(pattern: string) {
    await this.ensureConnection();
    const keys: string[] = [];
    let cursor = '0';

    do {
      const [nextCursor, batch] = await this.redis.scan(cursor, 'MATCH', pattern, 'COUNT', 100);
      cursor = nextCursor;
      keys.push(...batch);
    } while (cursor !== '0');

    if (keys.length) {
      await this.redis.del(...keys);
    }

    return keys.length;
  }

  async onModuleDestroy() {
    await this.redis.quit();
  }

  private async ensureConnection() {
    if (this.redis.status === 'wait') {
      await this.redis.connect();
    }
  }
}
