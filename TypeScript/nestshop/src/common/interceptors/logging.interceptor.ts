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
    const now = Date.now();

    return next.handle().pipe(
      tap(() => {
        const response = context.switchToHttp().getResponse();
        const statusCode = response.statusCode as number;
        const elapsed = Date.now() - now;

        this.logger.log(
          `${method} ${originalUrl} ${statusCode} ${elapsed}ms - ${ip} ${userAgent}`,
        );
      }),
    );
  }
}
