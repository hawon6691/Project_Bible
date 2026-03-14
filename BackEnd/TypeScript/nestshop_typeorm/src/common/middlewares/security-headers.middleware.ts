import { Request, Response, NextFunction } from 'express';

export function securityHeadersMiddleware(
  _request: Request,
  response: Response,
  next: NextFunction,
) {
  // 기본 보안 헤더를 명시적으로 적용해 브라우저 기본 보호 기능을 활성화한다.
  response.setHeader('X-Content-Type-Options', 'nosniff');
  response.setHeader('X-Frame-Options', 'DENY');
  response.setHeader('X-XSS-Protection', '1; mode=block');
  response.setHeader('Referrer-Policy', 'no-referrer');
  response.setHeader('Permissions-Policy', 'camera=(), microphone=(), geolocation=()');
  response.setHeader(
    'Strict-Transport-Security',
    'max-age=31536000; includeSubDomains',
  );
  response.setHeader('Cross-Origin-Resource-Policy', 'same-origin');
  response.setHeader('Cross-Origin-Opener-Policy', 'same-origin');

  next();
}
