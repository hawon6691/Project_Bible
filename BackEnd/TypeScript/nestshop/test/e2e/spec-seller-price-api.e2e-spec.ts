import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { PriceController } from '../../src/price/price.controller';
import { PriceService } from '../../src/price/price.service';
import { SellerController } from '../../src/seller/seller.controller';
import { SellerService } from '../../src/seller/seller.service';
import { ProductSpecController, SpecController } from '../../src/spec/spec.controller';
import { SpecService } from '../../src/spec/spec.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Spec Seller Price API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const specServiceMock = {
    findDefinitions: jest.fn().mockResolvedValue([{ id: 1, name: 'CPU' }]),
    createDefinition: jest.fn().mockResolvedValue({ id: 1, name: 'CPU' }),
    updateDefinition: jest.fn().mockResolvedValue({ id: 1, name: 'Processor' }),
    removeDefinition: jest.fn().mockResolvedValue({ message: '스펙 정의가 삭제되었습니다.' }),
    getProductSpecs: jest.fn().mockResolvedValue([{ name: 'CPU', value: 'i7' }, { name: 'RAM', value: '16GB' }]),
    setProductSpecs: jest.fn().mockResolvedValue([{ name: 'CPU', value: 'i7' }, { name: 'RAM', value: '16GB' }]),
  };
  const sellerServiceMock = {
    findAll: jest.fn().mockResolvedValue({ items: [{ id: 1, name: 'PB Mall' }], meta: { totalItems: 1 } }),
    findOne: jest.fn().mockResolvedValue({ id: 1, name: 'PB Mall' }),
    create: jest.fn().mockResolvedValue({ id: 1, name: 'PB Mall' }),
    update: jest.fn().mockResolvedValue({ id: 1, name: 'PB Mall Updated' }),
    remove: jest.fn().mockResolvedValue({ message: '판매처가 삭제되었습니다.' }),
  };
  const priceServiceMock = {
    getProductPrices: jest.fn().mockResolvedValue({ productId: 10, lowestPrice: 979000, entries: [{ id: 7 }] }),
    createPriceEntry: jest.fn().mockResolvedValue({ id: 7, price: 999000 }),
    updatePriceEntry: jest.fn().mockResolvedValue({ id: 7, price: 979000 }),
    removePriceEntry: jest.fn().mockResolvedValue({ message: '가격 정보가 삭제되었습니다.' }),
    getPriceHistory: jest.fn().mockResolvedValue({ productId: 10, items: [{ price: 999000 }] }),
    getMyAlerts: jest.fn().mockResolvedValue([{ id: 3, productName: 'PB Tablet' }]),
    createAlert: jest.fn().mockResolvedValue({ id: 3, targetPrice: 950000 }),
    removeAlert: jest.fn().mockResolvedValue({ removed: true }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [SpecController, ProductSpecController, SellerController, PriceController],
      providers: [
        { provide: SpecService, useValue: specServiceMock },
        { provide: SellerService, useValue: sellerServiceMock },
        { provide: PriceService, useValue: priceServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => app.close());

  it('should manage spec definitions and product specs', async () => {
    expect((await client.post('/specs/definitions', {
      categoryId: 1, name: 'CPU', type: 'SELECT', options: ['i5', 'i7'], sortOrder: 1,
    }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.get('/specs/definitions?categoryId=1')).status).toBe(200);
    expect((await client.put('/products/10/specs', {
      specs: [{ specDefinitionId: 1, value: 'i7' }, { specDefinitionId: 2, value: '16GB' }],
    }, { headers: authHeaders('ADMIN') })).status).toBe(200);
    expect((await client.get('/products/10/specs')).status).toBe(200);
    expect((await client.patch('/specs/definitions/1', { name: 'Processor' }, { headers: authHeaders('ADMIN') })).status).toBe(200);
    expect((await client.delete('/specs/definitions/1', { headers: authHeaders('ADMIN') })).status).toBe(200);
  });

  it('should manage sellers, prices, and price alerts', async () => {
    expect((await client.post('/sellers', {
      name: 'PB Mall', url: 'https://seller.example.com', description: 'seller',
    }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.get('/sellers?page=1&limit=10')).status).toBe(200);
    expect((await client.post('/products/10/prices', {
      sellerId: 1, price: 999000, shippingCost: 0, productUrl: 'https://seller.example.com/p/10',
    }, { headers: authHeaders('SELLER') })).status).toBe(201);
    expect((await client.get('/products/10/prices')).status).toBe(200);
    expect((await client.get('/products/10/price-history')).status).toBe(200);
    expect((await client.patch('/prices/7', { price: 979000, shippingCost: 2500 }, { headers: authHeaders('ADMIN') })).status).toBe(200);
    expect((await client.post('/price-alerts', { productId: 10, targetPrice: 950000 }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/price-alerts', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.delete('/price-alerts/3', { headers: authHeaders('USER') })).status).toBe(200);
  });
});
