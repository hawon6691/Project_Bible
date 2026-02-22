import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { Request, Response } from 'express';

@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(HttpExceptionFilter.name);

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const request = ctx.getRequest<Request>();
    const response = ctx.getResponse<Response>();

    let status = HttpStatus.INTERNAL_SERVER_ERROR;
    let errorCode = 'COMMON_003';
    let message = '서버 내부 오류가 발생했습니다.';
    let details: unknown = undefined;

    if (exception instanceof HttpException) {
      status = exception.getStatus();
      const exceptionResponse = exception.getResponse();

      if (typeof exceptionResponse === 'object' && exceptionResponse !== null) {
        const res = exceptionResponse as Record<string, unknown>;
        errorCode = (res.errorCode as string) ?? `HTTP_${status}`;
        message = (res.message as string) ?? exception.message;

        // class-validator 에러 배열은 통일된 응답 포맷으로 정리한다.
        if (Array.isArray(res.message)) {
          message = '입력값 검증에 실패했습니다.';
          details = res.message;
          errorCode = 'COMMON_001';
        }
      } else {
        message = exceptionResponse as string;
      }

      this.logHttpException(request, status, errorCode, message, exception);
    } else if (exception instanceof Error) {
      message = exception.message;
      this.logger.error(
        `${request.method} ${request.originalUrl} 500 COMMON_003 ${exception.message}`,
        exception.stack,
      );
    }

    const errorResponse = {
      success: false,
      error: {
        code: errorCode,
        message,
        ...(details ? { details } : {}),
      },
      // 하위 호환을 위해 기존 필드도 함께 유지한다.
      errorCode,
      message,
      ...(details ? { details } : {}),
      timestamp: new Date().toISOString(),
      path: request.url,
    };

    response.status(status).json(errorResponse);
  }

  private logHttpException(
    request: Request,
    status: number,
    errorCode: string,
    message: string,
    exception: HttpException,
  ) {
    const context = `${request.method} ${request.originalUrl} ${status} ${errorCode} ${message}`;

    if (status >= 500) {
      this.logger.error(context, exception.stack);
      return;
    }

    // 4xx는 클라이언트 입력/권한 이슈이므로 warn 레벨로 분리한다.
    this.logger.warn(context);
  }
}
