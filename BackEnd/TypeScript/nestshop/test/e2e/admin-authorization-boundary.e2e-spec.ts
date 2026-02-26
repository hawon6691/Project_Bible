import { CanActivate, Controller, ExecutionContext, Get, HttpStatus, Injectable, INestApplication, UseGuards } from '@nestjs/common';
import { Roles } from '../../src/common/decorators/roles.decorator';
import { UserRole } from '../../src/common/decorators/roles.decorator';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';

@Injectable()
class HeaderAuthGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const role = String(request.headers['x-user-role'] ?? '').trim();

    if (!role) {
      return false;
    }

    request.user = {
      sub: 1,
      role,
    };
    return true;
  }
}

@Controller('admin/auth-boundary')
@UseGuards(HeaderAuthGuard, RolesGuard)
class AdminAuthBoundaryController {
  @Get('metrics')
  @Roles(UserRole.ADMIN)
  getMetrics() {
    return {
      ok: true,
      message: 'admin only',
    };
  }
}

describe('Admin Authorization Boundary E2E', () => {
  let app: INestApplication;
  let baseUrl: string;

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [AdminAuthBoundaryController],
      providers: [HeaderAuthGuard, RolesGuard],
    });
    await app.listen(0);
    const address = app.getHttpServer().address() as { port: number };
    baseUrl = `http://127.0.0.1:${address.port}`;
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /admin/auth-boundary/metrics should return forbidden without header', async () => {
    const res = await fetch(`${baseUrl}/admin/auth-boundary/metrics`);
    const body = await res.json();
    expect(res.status).toBe(HttpStatus.FORBIDDEN);
    expect(body.success).toBe(false);
  });

  it('GET /admin/auth-boundary/metrics should return forbidden for non-admin', async () => {
    const res = await fetch(`${baseUrl}/admin/auth-boundary/metrics`, {
      headers: {
        'x-user-role': 'USER',
      },
    });
    const body = await res.json();
    expect(res.status).toBe(HttpStatus.FORBIDDEN);
    expect(body.success).toBe(false);
    expect(body.errorCode).toBe('AUTH_010');
  });

  it('GET /admin/auth-boundary/metrics should allow admin', async () => {
    const res = await fetch(`${baseUrl}/admin/auth-boundary/metrics`, {
      headers: {
        'x-user-role': 'ADMIN',
      },
    });
    const body = await res.json();
    expect(res.status).toBe(HttpStatus.OK);
    expect(body.success).toBe(true);
    expect(body.data.ok).toBe(true);
  });
});
