import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { UserController } from '../../src/user/user.controller';
import { UserService } from '../../src/user/user.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('User API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const userServiceMock = {
    getMe: jest.fn().mockResolvedValue({ id: 1, email: 'tester@example.com', name: '테스터' }),
    updateMe: jest.fn().mockImplementation(async (id, dto) => ({ id, ...dto })),
    deleteMe: jest.fn().mockResolvedValue({ deleted: true }),
    getProfile: jest.fn().mockResolvedValue({ id: 7, nickname: 'public-user', bio: 'hello' }),
    updateProfile: jest.fn().mockImplementation(async (id, dto) => ({ id, ...dto })),
    updateProfileImage: jest.fn().mockResolvedValue({ profileImageUrl: null }),
    findAll: jest.fn().mockResolvedValue({ items: [{ id: 1 }, { id: 2 }], total: 2 }),
    updateStatus: jest.fn().mockResolvedValue({ id: 2, status: 'SUSPENDED' }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [UserController],
      providers: [
        { provide: UserService, useValue: userServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /users/me and PUT /users/me should require auth and update profile fields', async () => {
    const unauthorized = await client.get('/users/me');
    expect(unauthorized.status).toBe(401);

    const me = await client.get('/users/me', { headers: authHeaders('USER') });
    expect(me.status).toBe(200);
    expect(me.body.data.email).toBe('tester@example.com');

    const update = await client.put(
      '/users/me',
      { name: '수정된 사용자', phone: '010-9999-9999', password: 'Password1!' },
      { headers: authHeaders('USER') },
    );
    expect(update.status).toBe(200);
    expect(update.body.data.name).toBe('수정된 사용자');
  });

  it('PATCH /users/me/profile and DELETE /users/me/profile-image should manage user profile', async () => {
    const profile = await client.patch(
      '/users/me/profile',
      { nickname: '새닉네임', bio: '소개글 수정' },
      { headers: authHeaders('USER') },
    );
    expect(profile.status).toBe(200);
    expect(profile.body.data.nickname).toBe('새닉네임');

    const deleteImage = await client.delete('/users/me/profile-image', {
      headers: authHeaders('USER'),
    });
    expect(deleteImage.status).toBe(200);
    expect(deleteImage.body.data.profileImageUrl).toBeNull();
  });

  it('GET /users/profile/:id and DELETE /users/me should cover public profile and withdrawal', async () => {
    const profile = await client.get('/users/profile/7');
    expect(profile.status).toBe(200);
    expect(profile.body.data.nickname).toBe('public-user');

    const remove = await client.delete('/users/me', { headers: authHeaders('USER') });
    expect(remove.status).toBe(200);
    expect(remove.body.data.deleted).toBe(true);
  });

  it('admin endpoints should reject non-admin and allow admin', async () => {
    const forbidden = await client.get('/users?page=1&limit=10', { headers: authHeaders('USER') });
    expect(forbidden.status).toBe(403);

    const list = await client.get('/users?page=1&limit=10', { headers: authHeaders('ADMIN') });
    expect(list.status).toBe(200);
    expect(Array.isArray(list.body.data.items)).toBe(true);

    const status = await client.patch(
      '/users/2/status',
      { status: 'SUSPENDED' },
      { headers: authHeaders('ADMIN') },
    );
    expect(status.status).toBe(200);
    expect(status.body.data.status).toBe('SUSPENDED');
  });
});
