import {
  CanActivate,
  ExecutionContext,
  HttpStatus,
  Injectable,
} from '@nestjs/common';
import { Request } from 'express';
import { BusinessException } from '../exceptions/business.exception';

interface RateWindow {
  count: number;
  resetAt: number;
}

@Injectable()
export class RateLimitGuard implements CanActivate {
  private readonly store = new Map<string, RateWindow>();
  private readonly windowMs = 60_000;
  private readonly defaultLimitPerWindow = 60;
  private readonly authLimitPerWindow = 10;

  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest<Request>();
    const path = request.path ?? request.url ?? '';

    // Swagger/정적 리소스 등 개발 편의 경로는 제한 대상에서 제외한다.
    if (this.isExcludedPath(path)) {
      return true;
    }

    const limit = this.resolveLimit(path);
    const key = this.buildKey(request, path);
    const now = Date.now();
    const window = this.store.get(key);

    if (!window || now >= window.resetAt) {
      this.store.set(key, {
        count: 1,
        resetAt: now + this.windowMs,
      });
      this.cleanupExpiredWindows(now);
      return true;
    }

    if (window.count >= limit) {
      throw new BusinessException(
        'TOO_MANY_REQUESTS',
        HttpStatus.TOO_MANY_REQUESTS,
      );
    }

    window.count += 1;
    return true;
  }

  private resolveLimit(path: string): number {
    return path.includes('/auth')
      ? this.authLimitPerWindow
      : this.defaultLimitPerWindow;
  }

  private buildKey(request: Request, path: string): string {
    const ip = request.ip ?? request.socket.remoteAddress ?? 'unknown';
    const bucket = path.includes('/auth') ? 'auth' : 'default';
    return `${bucket}:${ip}`;
  }

  private isExcludedPath(path: string): boolean {
    return (
      path.startsWith('/docs') ||
      path.includes('/docs') ||
      path.endsWith('/favicon.ico')
    );
  }

  private cleanupExpiredWindows(now: number) {
    // 인메모리 키 누수를 줄이기 위해 오래된 윈도우를 주기적으로 제거한다.
    if (this.store.size < 5000) {
      return;
    }

    for (const [key, window] of this.store.entries()) {
      if (window.resetAt <= now) {
        this.store.delete(key);
      }
    }
  }
}
