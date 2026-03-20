import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { ReviewController } from '../../src/review/review.controller';
import { ReviewService } from '../../src/review/review.service';
import { WishlistController } from '../../src/wishlist/wishlist.controller';
import { WishlistService } from '../../src/wishlist/wishlist.service';
import { PointController } from '../../src/point/point.controller';
import { PointService } from '../../src/point/point.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Review Wishlist Point API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const reviewServiceMock = {
    findByProduct: jest.fn().mockResolvedValue({ items: [{ id: 1, rating: 5 }], total: 1 }),
    create: jest.fn().mockResolvedValue({ id: 1, rating: 5, content: '좋아요' }),
    update: jest.fn().mockResolvedValue({ id: 1, rating: 4, content: '수정됨' }),
    remove: jest.fn().mockResolvedValue({ removed: true }),
  };

  const wishlistServiceMock = {
    findMyWishlist: jest.fn().mockResolvedValue({ items: [{ productId: 10 }], total: 1 }),
    toggle: jest.fn().mockResolvedValue({ productId: 10, active: true }),
    remove: jest.fn().mockResolvedValue({ removed: true }),
  };

  const pointServiceMock = {
    getBalance: jest.fn().mockResolvedValue({ balance: 3000 }),
    getTransactions: jest.fn().mockResolvedValue({ items: [{ id: 1, amount: 1000 }], total: 1 }),
    adminGrant: jest.fn().mockResolvedValue({ granted: true, amount: 1000 }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [ReviewController, WishlistController, PointController],
      providers: [
        { provide: ReviewService, useValue: reviewServiceMock },
        { provide: WishlistService, useValue: wishlistServiceMock },
        { provide: PointService, useValue: pointServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('reviews should support public list and authenticated CRUD', async () => {
    const list = await client.get('/products/10/reviews?page=1&limit=10');
    expect(list.status).toBe(200);

    const invalid = await client.post(
      '/products/10/reviews',
      { orderId: 5, rating: 6, content: 'bad' },
      { headers: authHeaders('USER') },
    );
    expect(invalid.status).toBe(400);

    const created = await client.post(
      '/products/10/reviews',
      { orderId: 5, rating: 5, content: '좋아요' },
      { headers: authHeaders('USER') },
    );
    expect(created.status).toBe(201);

    const updated = await client.patch(
      '/reviews/1',
      { rating: 4, content: '수정됨' },
      { headers: authHeaders('USER') },
    );
    expect(updated.status).toBe(200);

    const removed = await client.delete('/reviews/1', { headers: authHeaders('USER') });
    expect(removed.status).toBe(200);
  });

  it('wishlist and point endpoints should reflect member and admin roles', async () => {
    const wishlist = await client.get('/wishlist?page=1&limit=10', { headers: authHeaders('USER') });
    expect(wishlist.status).toBe(200);

    const toggle = await client.post('/wishlist/10', {}, { headers: authHeaders('USER') });
    expect(toggle.status).toBe(201);
    expect(toggle.body.data.active).toBe(true);

    const balance = await client.get('/points/balance', { headers: authHeaders('USER') });
    expect(balance.status).toBe(200);
    expect(balance.body.data.balance).toBe(3000);

    const forbidden = await client.post(
      '/admin/points/grant',
      { userId: 1, amount: 1000 },
      { headers: authHeaders('USER') },
    );
    expect(forbidden.status).toBe(403);

    const grant = await client.post(
      '/admin/points/grant',
      { userId: 1, amount: 1000, description: '리뷰 보상' },
      { headers: authHeaders('ADMIN') },
    );
    expect(grant.status).toBe(201);
    expect(grant.body.data.granted).toBe(true);
  });
});
