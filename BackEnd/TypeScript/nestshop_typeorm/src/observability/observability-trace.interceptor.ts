import { CallHandler, ExecutionContext, Injectable, NestInterceptor } from '@nestjs/common';
import { Observable, tap } from 'rxjs';
import { Request, Response } from 'express';
import { getOrCreateRequestId } from '../common/utils/request-id.util';
import { ObservabilityService } from './observability.service';

@Injectable()
export class ObservabilityTraceInterceptor implements NestInterceptor {
  constructor(private readonly observabilityService: ObservabilityService) {}

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const http = context.switchToHttp();
    const request = http.getRequest<Request & { user?: { sub?: number } }>();
    const response = http.getResponse<Response>();
    const startedAt = Date.now();
    const requestId = getOrCreateRequestId(request, response);

    return next.handle().pipe(
      tap(() => {
        this.observabilityService.recordTrace({
          requestId,
          method: request.method,
          path: request.originalUrl ?? request.url ?? '',
          statusCode: response.statusCode,
          durationMs: Date.now() - startedAt,
          ip: request.ip ?? null,
          userId: request.user?.sub ?? null,
          timestamp: new Date().toISOString(),
        });
      }),
    );
  }
}
