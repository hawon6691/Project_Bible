import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
  Logger,
} from '@nestjs/common';
import { Observable, tap } from 'rxjs';
import { Request, Response } from 'express';
import { getOrCreateRequestId } from '../utils/request-id.util';

type AuthenticatedRequest = Request & {
  user?: {
    sub?: number;
  };
};

@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  private readonly logger = new Logger('HTTP');

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const http = context.switchToHttp();
    const request = http.getRequest<AuthenticatedRequest>();
    const response = http.getResponse<Response>();
    const requestId = getOrCreateRequestId(request, response);

    const { method, originalUrl, ip } = request;
    const userAgent = request.get('user-agent') ?? '';
    const userId = request.user?.sub;
    const now = Date.now();

    return next.handle().pipe(
      tap(() => {
        const statusCode = response.statusCode as number;
        const elapsed = Date.now() - now;
        const message = `${method} ${originalUrl} ${statusCode} ${elapsed}ms - ${ip} ${userAgent} user:${userId ?? 'anonymous'} requestId:${requestId}`;

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
