import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
  Logger,
} from '@nestjs/common';
import { Observable, tap } from 'rxjs';
import { Request } from 'express';

@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  private readonly logger = new Logger('HTTP');

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const request = context.switchToHttp().getRequest<Request>();
    const { method, originalUrl, ip } = request;
    const userAgent = request.get('user-agent') ?? '';
    const userId = (request as Request & { user?: { sub?: number } }).user?.sub;
    const now = Date.now();

    return next.handle().pipe(
      tap(() => {
        const response = context.switchToHttp().getResponse();
        const statusCode = response.statusCode as number;
        const elapsed = Date.now() - now;
        const message = `${method} ${originalUrl} ${statusCode} ${elapsed}ms - ${ip} ${userAgent} user:${userId ?? 'anonymous'}`;

        if (statusCode >= 500) {
          this.logger.error(message);
          return;
        }
        if (statusCode >= 400) {
          this.logger.warn(message);
          return;
        }

        // 정상 요청은 debug 레벨로 남겨 dev 환경에서만 상세 추적한다.
        this.logger.debug(message);
      }),
    );
  }
}
