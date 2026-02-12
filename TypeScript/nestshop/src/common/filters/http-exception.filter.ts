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

        // class-validator 에러 배열 처리
        if (Array.isArray(res.message)) {
          message = '입력값 검증에 실패했습니다.';
          details = res.message;
          errorCode = 'COMMON_001';
        }
      } else {
        message = exceptionResponse as string;
      }
    } else if (exception instanceof Error) {
      message = exception.message;
      this.logger.error(
        `Unhandled Error: ${exception.message}`,
        exception.stack,
      );
    }

    const errorResponse = {
      success: false,
      errorCode,
      message,
      ...(details ? { details } : {}),
      timestamp: new Date().toISOString(),
      path: request.url,
    };

    response.status(status).json(errorResponse);
  }
}
