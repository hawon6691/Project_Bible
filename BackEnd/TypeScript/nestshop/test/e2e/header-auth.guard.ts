import { CanActivate, ExecutionContext, HttpStatus, Injectable } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { JwtPayload } from '../../src/common/decorators/current-user.decorator';
import { IS_PUBLIC_KEY } from '../../src/common/decorators/public.decorator';
import { BusinessException } from '../../src/common/exceptions/business.exception';

@Injectable()
export class HeaderAuthGuard implements CanActivate {
  constructor(private readonly reflector: Reflector) {}

  canActivate(context: ExecutionContext): boolean {
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);

    if (isPublic) {
      return true;
    }

    const request = context.switchToHttp().getRequest();
    const userId = Number(request.headers['x-user-id'] ?? 1);
    const role = String(request.headers['x-user-role'] ?? '').trim();
    const email = String(request.headers['x-user-email'] ?? 'tester@example.com').trim();

    if (!role) {
      throw new BusinessException('AUTH_UNAUTHORIZED', HttpStatus.UNAUTHORIZED);
    }

    request.user = {
      sub: Number.isFinite(userId) ? userId : 1,
      role,
      email,
    } satisfies JwtPayload;

    return true;
  }
}

export function authHeaders(
  role: 'USER' | 'SELLER' | 'ADMIN',
  overrides: Partial<Record<'x-user-id' | 'x-user-email', string>> = {},
) {
  return {
    'x-user-role': role,
    'x-user-id': overrides['x-user-id'] ?? '1',
    'x-user-email': overrides['x-user-email'] ?? 'tester@example.com',
  };
}
