import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { OrderController } from '../../src/order/order.controller';
import { OrderService } from '../../src/order/order.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Order Payment API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const orderServiceMock = {
    create: jest.fn().mockResolvedValue({ id: 101, status: 'PENDING', totalAmount: 250000 }),
    findMyOrders: jest.fn().mockResolvedValue({ items: [{ id: 101, status: 'PENDING' }], total: 1 }),
    findOne: jest.fn().mockResolvedValue({ id: 101, items: [{ productId: 10, quantity: 1 }] }),
    cancel: jest.fn().mockResolvedValue({ id: 101, status: 'CANCELLED' }),
    requestReturn: jest.fn().mockResolvedValue({ id: 101, status: 'RETURN_REQUESTED' }),
    requestPayment: jest.fn().mockResolvedValue({ id: 201, orderId: 101, status: 'PAID' }),
    getPayment: jest.fn().mockResolvedValue({ id: 201, status: 'PAID' }),
    refundPayment: jest.fn().mockResolvedValue({ id: 201, status: 'REFUNDED' }),
    findAllOrders: jest.fn().mockResolvedValue({ items: [{ id: 101 }], total: 1 }),
    updateStatus: jest.fn().mockResolvedValue({ id: 101, status: 'SHIPPED' }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [OrderController],
      providers: [
        { provide: OrderService, useValue: orderServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('POST /orders should validate payload and create order for authenticated user', async () => {
    const invalid = await client.post(
      '/orders',
      { addressId: 1, items: [{ productId: 10, sellerId: 20, quantity: 0 }] },
      { headers: authHeaders('USER') },
    );
    expect(invalid.status).toBe(400);

    const created = await client.post(
      '/orders',
      { addressId: 1, items: [{ productId: 10, sellerId: 20, quantity: 1 }], usePoint: 0 },
      { headers: authHeaders('USER') },
    );
    expect(created.status).toBe(201);
    expect(created.body.data.id).toBe(101);
  });

  it('GET /orders, /orders/:id and lifecycle endpoints should work', async () => {
    const list = await client.get('/orders?page=1&limit=10', { headers: authHeaders('USER') });
    expect(list.status).toBe(200);
    expect(Array.isArray(list.body.data.items)).toBe(true);

    const detail = await client.get('/orders/101', { headers: authHeaders('USER') });
    expect(detail.status).toBe(200);
    expect(detail.body.data.id).toBe(101);

    const cancel = await client.post('/orders/101/cancel', {}, { headers: authHeaders('USER') });
    expect(cancel.status).toBe(200);
    expect(cancel.body.data.status).toBe('CANCELLED');

    const returnRequest = await client.post('/orders/101/return-request', {}, { headers: authHeaders('USER') });
    expect(returnRequest.status).toBe(200);
    expect(returnRequest.body.data.status).toBe('RETURN_REQUESTED');
  });

  it('payment and admin order endpoints should enforce role boundaries', async () => {
    const payment = await client.post(
      '/payments',
      { orderId: 101, method: 'CARD', amount: 250000 },
      { headers: authHeaders('USER') },
    );
    expect(payment.status).toBe(201);
    expect(payment.body.data.status).toBe('PAID');

    const paymentDetail = await client.get('/payments/201', { headers: authHeaders('USER') });
    expect(paymentDetail.status).toBe(200);

    const forbidden = await client.get('/admin/orders?page=1&limit=10', { headers: authHeaders('USER') });
    expect(forbidden.status).toBe(403);

    const adminOrders = await client.get('/admin/orders?page=1&limit=10', { headers: authHeaders('ADMIN') });
    expect(adminOrders.status).toBe(200);

    const adminStatus = await client.patch(
      '/admin/orders/101/status',
      { status: 'SHIPPED' },
      { headers: authHeaders('ADMIN') },
    );
    expect(adminStatus.status).toBe(200);
    expect(adminStatus.body.data.status).toBe('SHIPPED');
  });
});
