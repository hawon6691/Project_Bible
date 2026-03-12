import { APP_GUARD } from '@nestjs/core';
import { INestApplication, HttpStatus } from '@nestjs/common';
import { CategoryController } from '../../src/category/category.controller';
import { CategoryService } from '../../src/category/category.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';
import { BusinessException } from '../../src/common/exceptions/business.exception';

describe('Category API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const categoryServiceMock = {
    findAllTree: jest.fn().mockResolvedValue([{ id: 1, name: '노트북', children: [] }]),
    findOne: jest.fn().mockResolvedValue({ id: 1, name: '노트북' }),
    create: jest.fn().mockImplementation(async (dto) => ({ id: 2, ...dto })),
    update: jest.fn().mockImplementation(async (id, dto) => ({ id, ...dto })),
    remove: jest.fn().mockImplementation(async (id) => {
      if (id === 1) {
        throw new BusinessException('CATEGORY_HAS_CHILDREN', HttpStatus.BAD_REQUEST);
      }
      return { removed: true };
    }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [CategoryController],
      providers: [
        { provide: CategoryService, useValue: categoryServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('public endpoints should return category tree and detail', async () => {
    const tree = await client.get('/categories');
    expect(tree.status).toBe(200);
    expect(Array.isArray(tree.body.data)).toBe(true);

    const detail = await client.get('/categories/1');
    expect(detail.status).toBe(200);
    expect(detail.body.data.name).toBe('노트북');
  });

  it('admin should manage categories while non-admin is forbidden', async () => {
    const forbidden = await client.post('/categories', { name: '태블릿' }, { headers: authHeaders('USER') });
    expect(forbidden.status).toBe(403);

    const created = await client.post('/categories', { name: '태블릿' }, { headers: authHeaders('ADMIN') });
    expect(created.status).toBe(201);
    expect(created.body.data.id).toBe(2);

    const updated = await client.patch('/categories/2', { name: '태블릿 PC' }, { headers: authHeaders('ADMIN') });
    expect(updated.status).toBe(200);
    expect(updated.body.data.name).toBe('태블릿 PC');
  });

  it('DELETE /categories/:id should guard child categories', async () => {
    const blocked = await client.delete('/categories/1', { headers: authHeaders('ADMIN') });
    expect(blocked.status).toBe(400);
    expect(blocked.body.success).toBe(false);

    const removed = await client.delete('/categories/2', { headers: authHeaders('ADMIN') });
    expect(removed.status).toBe(200);
    expect(removed.body.data.removed).toBe(true);
  });
});
