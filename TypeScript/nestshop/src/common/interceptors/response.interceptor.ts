import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
} from '@nestjs/common';
import { Observable, map } from 'rxjs';

export interface SuccessResponse<T> {
  success: true;
  data: T;
  meta?: Record<string, unknown>;
  timestamp: string;
}

@Injectable()
export class ResponseInterceptor<T>
  implements NestInterceptor<T, SuccessResponse<unknown>>
{
  intercept(
    context: ExecutionContext,
    next: CallHandler,
  ): Observable<SuccessResponse<unknown>> {
    return next.handle().pipe(
      map((payload) => {
        const { data, meta } = this.normalizePayload(payload);

        return {
          success: true as const,
          data,
          ...(meta ? { meta } : {}),
          timestamp: new Date().toISOString(),
        };
      }),
    );
  }

  private normalizePayload(payload: unknown): {
    data: unknown;
    meta?: Record<string, unknown>;
  } {
    if (!this.isRecord(payload)) {
      return { data: payload };
    }

    if (!this.isRecord(payload.meta)) {
      return { data: payload };
    }

    const meta = this.normalizeMeta(payload.meta);

    // 기존 PaginationResponseDto(items + meta) 응답을 스펙 형태로 맞춘다.
    if ('items' in payload) {
      return { data: payload.items, meta };
    }

    if ('data' in payload) {
      return { data: payload.data, meta };
    }

    return { data: payload, meta };
  }

  private normalizeMeta(meta: Record<string, unknown>): Record<string, unknown> {
    const page = this.toNumber(meta.page ?? meta.currentPage, 1);
    const limit = this.toNumber(meta.limit ?? meta.itemsPerPage, 20);
    const totalCount = this.toNumber(meta.totalCount ?? meta.totalItems, 0);
    const totalPages = this.toNumber(meta.totalPages, 1);

    return {
      ...meta,
      page,
      limit,
      totalCount,
      totalPages,
    };
  }

  private toNumber(value: unknown, fallback: number): number {
    const num = Number(value);
    return Number.isFinite(num) ? num : fallback;
  }

  private isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null;
  }
}
