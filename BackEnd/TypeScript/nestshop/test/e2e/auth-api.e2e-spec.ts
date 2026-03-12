import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { AuthController } from '../../src/auth/auth.controller';
import { AuthService } from '../../src/auth/auth.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Auth API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const authServiceMock = {
    signup: jest.fn().mockImplementation(async (dto) => ({
      user: { id: 1, email: dto.email, name: dto.name, phone: dto.phone },
      emailVerificationRequired: true,
    })),
    verifyEmail: jest.fn().mockResolvedValue({ verified: true }),
    resendVerification: jest.fn().mockResolvedValue({ resent: true }),
    login: jest.fn().mockResolvedValue({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      user: { id: 1, email: 'tester@example.com', role: 'USER' },
    }),
    logout: jest.fn().mockResolvedValue({ loggedOut: true }),
    refresh: jest.fn().mockResolvedValue({ accessToken: 'new-access-token', refreshToken: 'new-refresh-token' }),
    requestPasswordReset: jest.fn().mockResolvedValue({ requested: true }),
    verifyResetCode: jest.fn().mockResolvedValue({ verified: true }),
    resetPassword: jest.fn().mockResolvedValue({ changed: true }),
    getSocialAuthUrl: jest.fn().mockResolvedValue({ provider: 'google', authUrl: 'https://example.com/oauth' }),
    socialCallback: jest.fn().mockResolvedValue({ registered: false, accessToken: 'social-access-token' }),
    completeSocialSignup: jest.fn().mockResolvedValue({ completed: true }),
    linkSocialAccount: jest.fn().mockResolvedValue({ linked: true }),
    unlinkSocialAccount: jest.fn().mockResolvedValue({ unlinked: true }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [AuthController],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('POST /auth/signup should validate payload and return created user', async () => {
    const invalid = await client.post('/auth/signup', {
      email: 'bad-email',
      password: '1234',
      name: 'A',
      phone: '0101234',
    });
    expect(invalid.status).toBe(400);
    expect(invalid.body.success).toBe(false);

    const success = await client.post('/auth/signup', {
      email: 'tester@example.com',
      password: 'Password1!',
      name: '테스터',
      phone: '010-1234-5678',
    });
    expect(success.status).toBe(201);
    expect(success.body.success).toBe(true);
    expect(success.body.data.user.email).toBe('tester@example.com');
  });

  it('POST /auth/verify-email and /auth/login should complete auth flow', async () => {
    const verify = await client.post('/auth/verify-email', {
      email: 'tester@example.com',
      code: '123456',
    });
    expect(verify.status).toBe(200);
    expect(verify.body.data.verified).toBe(true);

    const login = await client.post('/auth/login', {
      email: 'tester@example.com',
      password: 'Password1!',
    });
    expect(login.status).toBe(200);
    expect(login.body.data.accessToken).toBe('access-token');
  });

  it('POST /auth/refresh and /auth/logout should require proper auth flow', async () => {
    const refresh = await client.post('/auth/refresh', {
      refreshToken: 'refresh-token',
    });
    expect(refresh.status).toBe(200);
    expect(refresh.body.data.accessToken).toBe('new-access-token');

    const unauthorized = await client.post('/auth/logout', {});
    expect(unauthorized.status).toBe(401);
    expect(unauthorized.body.success).toBe(false);

    const logout = await client.post('/auth/logout', {}, { headers: authHeaders('USER') });
    expect(logout.status).toBe(200);
    expect(logout.body.data.loggedOut).toBe(true);
  });

  it('GET /auth/:provider and social linking endpoints should work', async () => {
    const socialUrl = await client.get('/auth/google?state=abc');
    expect(socialUrl.status).toBe(200);
    expect(socialUrl.body.data.authUrl).toContain('https://');

    const callback = await client.post('/auth/google/callback', {
      code: 'oauth-code',
      state: 'abc',
    });
    expect(callback.status).toBe(200);
    expect(callback.body.data.accessToken).toBe('social-access-token');

    const link = await client.post(
      '/auth/social/link',
      { provider: 'google', code: 'provider-code', mockEmail: 'social@example.com' },
      { headers: authHeaders('USER') },
    );
    expect(link.status).toBe(200);
    expect(link.body.data.linked).toBe(true);

    const unlink = await client.delete('/auth/social/google', { headers: authHeaders('USER') });
    expect(unlink.status).toBe(200);
    expect(unlink.body.data.unlinked).toBe(true);
  });
});
