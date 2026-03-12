import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { CartController } from '../../src/cart/cart.controller';
import { CartService } from '../../src/cart/cart.service';
import { AddressController } from '../../src/address/address.controller';
import { AddressService } from '../../src/address/address.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Cart Address API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const addressServiceMock = {
    findMyAddresses: jest.fn().mockResolvedValue([{ id: 1, recipientName: '홍길동' }]),
    create: jest.fn().mockImplementation(async (_userId, dto) => ({ id: 1, ...dto })),
    update: jest.fn().mockImplementation(async (userId, id, dto) => ({ userId, id, ...dto })),
    remove: jest.fn().mockResolvedValue({ removed: true }),
  };

  const cartServiceMock = {
    getCart: jest.fn().mockResolvedValue({ items: [{ id: 11, quantity: 1 }] }),
    addItem: jest.fn().mockResolvedValue({ itemId: 11, quantity: 1 }),
    updateQuantity: jest.fn().mockResolvedValue({ itemId: 11, quantity: 2 }),
    removeItem: jest.fn().mockResolvedValue({ removed: true }),
    clearCart: jest.fn().mockResolvedValue({ cleared: true }),
    getGuestCart: jest.fn().mockResolvedValue({ items: [] }),
    addGuestItem: jest.fn().mockResolvedValue({ itemId: 'g-1', quantity: 1 }),
    updateGuestQuantity: jest.fn().mockResolvedValue({ itemId: 'g-1', quantity: 2 }),
    removeGuestItem: jest.fn().mockResolvedValue({ removed: true }),
    clearGuestCart: jest.fn().mockResolvedValue({ cleared: true }),
    mergeGuestCartToUser: jest.fn().mockResolvedValue({ merged: true }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [AddressController, CartController],
      providers: [
        { provide: AddressService, useValue: addressServiceMock },
        { provide: CartService, useValue: cartServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('addresses should support list, create, update, and delete', async () => {
    const list = await client.get('/addresses', { headers: authHeaders('USER') });
    expect(list.status).toBe(200);

    const invalid = await client.post(
      '/addresses',
      { recipientName: '', phone: '0101234', zipCode: '12', address: '' },
      { headers: authHeaders('USER') },
    );
    expect(invalid.status).toBe(400);

    const created = await client.post(
      '/addresses',
      {
        recipientName: '홍길동',
        phone: '010-1234-5678',
        zipCode: '06236',
        address: '서울특별시 강남구 테헤란로 123',
      },
      { headers: authHeaders('USER') },
    );
    expect(created.status).toBe(201);

    const updated = await client.patch(
      '/addresses/1',
      { addressDetail: '101동 202호' },
      { headers: authHeaders('USER') },
    );
    expect(updated.status).toBe(200);

    const removed = await client.delete('/addresses/1', { headers: authHeaders('USER') });
    expect(removed.status).toBe(200);
  });

  it('cart should support member and guest flows', async () => {
    const cart = await client.get('/cart', { headers: authHeaders('USER') });
    expect(cart.status).toBe(200);

    const add = await client.post('/cart', { productId: 10, sellerId: 20, quantity: 1 }, { headers: authHeaders('USER') });
    expect(add.status).toBe(201);

    const patch = await client.patch('/cart/11', { quantity: 2 }, { headers: authHeaders('USER') });
    expect(patch.status).toBe(200);

    const guest = await client.get('/cart/guest', { headers: { 'x-cart-key': 'guest-1' } });
    expect(guest.status).toBe(200);

    const merge = await client.post('/cart/guest/merge', { guestCartKey: 'guest-1' }, { headers: authHeaders('USER') });
    expect(merge.status).toBe(200);
    expect(merge.body.data.merged).toBe(true);
  });
});
