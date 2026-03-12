import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { ProductController } from '../../src/product/product.controller';
import { ProductService } from '../../src/product/product.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Product API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const productServiceMock = {
    findAll: jest.fn().mockResolvedValue({ items: [{ id: 10, name: '테스트 노트북' }], totalItems: 1, totalPages: 1 }),
    findOne: jest.fn().mockResolvedValue({ id: 10, name: '테스트 노트북', price: 1500000 }),
    create: jest.fn().mockImplementation(async (dto) => ({ id: 11, ...dto })),
    update: jest.fn().mockImplementation(async (id, dto) => ({ id, ...dto })),
    remove: jest.fn().mockResolvedValue({ removed: true }),
    addOption: jest.fn().mockResolvedValue({ id: 31, name: '색상', values: ['실버'] }),
    updateOption: jest.fn().mockResolvedValue({ id: 31, name: '색상', values: ['실버', '블랙'] }),
    removeOption: jest.fn().mockResolvedValue({ removed: true }),
    addImage: jest.fn().mockResolvedValue({ id: 41, url: 'https://cdn.example.com/p1.png' }),
    removeImage: jest.fn().mockResolvedValue({ removed: true }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [ProductController],
      providers: [
        { provide: ProductService, useValue: productServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /products and /products/:id should expose public product data', async () => {
    const list = await client.get('/products?page=1&limit=10');
    expect(list.status).toBe(200);
    expect(Array.isArray(list.body.data.items)).toBe(true);

    const detail = await client.get('/products/10');
    expect(detail.status).toBe(200);
    expect(detail.body.data.id).toBe(10);
  });

  it('POST /products should validate payload and require admin role', async () => {
    const invalid = await client.post(
      '/products',
      { name: 'x'.repeat(201), description: '설명', price: -1, stock: -1, categoryId: 1 },
      { headers: authHeaders('ADMIN') },
    );
    expect(invalid.status).toBe(400);

    const forbidden = await client.post(
      '/products',
      { name: '테스트 노트북', description: '설명', price: 1000, stock: 1, categoryId: 1 },
      { headers: authHeaders('USER') },
    );
    expect(forbidden.status).toBe(403);

    const created = await client.post(
      '/products',
      { name: '테스트 노트북', description: '설명', price: 1000, stock: 1, categoryId: 1 },
      { headers: authHeaders('ADMIN') },
    );
    expect(created.status).toBe(201);
    expect(created.body.data.id).toBe(11);
  });

  it('admin should manage product, options, and images', async () => {
    const update = await client.patch(
      '/products/11',
      { name: '수정된 상품명' },
      { headers: authHeaders('ADMIN') },
    );
    expect(update.status).toBe(200);
    expect(update.body.data.name).toBe('수정된 상품명');

    const option = await client.post(
      '/products/11/options',
      { name: '색상', values: ['실버'] },
      { headers: authHeaders('ADMIN') },
    );
    expect(option.status).toBe(201);

    const image = await client.post(
      '/products/11/images',
      { url: 'https://cdn.example.com/p1.png', isMain: true, sortOrder: 0 },
      { headers: authHeaders('ADMIN') },
    );
    expect(image.status).toBe(201);

    const remove = await client.delete('/products/11', { headers: authHeaders('ADMIN') });
    expect(remove.status).toBe(200);
    expect(remove.body.data.removed).toBe(true);
  });
});
