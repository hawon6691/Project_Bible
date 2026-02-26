import { HttpException, HttpStatus } from '@nestjs/common';
import { ERROR_CODES, ErrorCode } from '../constants/error-codes';

export class BusinessException extends HttpException {
  readonly errorCode: string;

  constructor(
    errorKey: ErrorCode,
    statusCode: HttpStatus = HttpStatus.BAD_REQUEST,
    overrideMessage?: string,
  ) {
    const error = ERROR_CODES[errorKey];
    const message = overrideMessage ?? error.message;

    super(
      {
        success: false,
        errorCode: error.code,
        message,
        timestamp: new Date().toISOString(),
      },
      statusCode,
    );

    this.errorCode = error.code;
  }
}
