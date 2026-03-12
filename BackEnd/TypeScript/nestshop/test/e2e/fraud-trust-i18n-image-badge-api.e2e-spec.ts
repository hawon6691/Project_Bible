import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { BadgeController } from '../../src/badge/badge.controller';
import { BadgeService } from '../../src/badge/badge.service';
import { FraudController } from '../../src/fraud/fraud.controller';
import { FraudService } from '../../src/fraud/fraud.service';
import { I18nController } from '../../src/i18n/i18n.controller';
import { I18nService } from '../../src/i18n/i18n.service';
import { ImageController } from '../../src/image/image.controller';
import { ImageService } from '../../src/image/image.service';
import { TrustController } from '../../src/trust/trust.controller';
import { TrustService } from '../../src/trust/trust.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Fraud Trust I18n Image Badge API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;
  const fraudServiceMock = {
    getRealPrice: jest.fn().mockResolvedValue({ totalPrice: 503000 }),
    detectAnomalies: jest.fn().mockResolvedValue({ items: [{ id: 1, priceEntryId: 7 }] }),
    getFlags: jest.fn().mockResolvedValue([{ id: 1 }]),
    getAlerts: jest.fn().mockResolvedValue([{ id: 1 }]),
    approveAlert: jest.fn().mockResolvedValue({ message: '이상 가격 알림이 승인되었습니다.' }),
  };
  const trustServiceMock = {
    getCurrentScore: jest.fn().mockResolvedValue({ sellerName: 'PB Seller' }),
    getHistory: jest.fn().mockResolvedValue([{ score: 90 }]),
    recalculateScore: jest.fn().mockResolvedValue({ recalculated: true }),
  };
  const i18nServiceMock = {
    upsertTranslation: jest.fn().mockResolvedValue({ id: 1, value: 'Lowest Price' }),
    getTranslations: jest.fn().mockResolvedValue([{ value: 'Lowest Price' }]),
    deleteTranslation: jest.fn().mockResolvedValue({ deleted: true }),
    upsertExchangeRate: jest.fn().mockResolvedValue({ id: 1 }),
    getExchangeRates: jest.fn().mockResolvedValue([{ baseCurrency: 'KRW' }]),
    convertAmount: jest.fn().mockResolvedValue({ rate: 0.000748 }),
  };
  const imageServiceMock = {
    getVariants: jest.fn().mockResolvedValue([{ type: 'THUMBNAIL' }]),
    remove: jest.fn().mockResolvedValue({ deleted: true }),
  };
  const badgeServiceMock = {
    create: jest.fn().mockResolvedValue({ id: 1, name: '리뷰 마스터' }),
    getAllBadges: jest.fn().mockResolvedValue([{ name: '리뷰 마스터' }]),
    grant: jest.fn().mockResolvedValue({ granted: true }),
    getMyBadges: jest.fn().mockResolvedValue([{ badge: { name: '리뷰 마스터' } }]),
    getUserBadges: jest.fn().mockResolvedValue([{ badge: { name: '리뷰 마스터' } }]),
    update: jest.fn().mockResolvedValue({ rarity: 'RARE' }),
    revoke: jest.fn().mockResolvedValue({ revoked: true }),
    remove: jest.fn().mockResolvedValue({ removed: true }),
  };
  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [FraudController, TrustController, I18nController, ImageController, BadgeController],
      providers: [
        { provide: FraudService, useValue: fraudServiceMock },
        { provide: TrustService, useValue: trustServiceMock },
        { provide: I18nService, useValue: i18nServiceMock },
        { provide: ImageService, useValue: imageServiceMock },
        { provide: BadgeService, useValue: badgeServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });
  afterAll(async () => app.close());

  it('should cover fraud, trust, i18n, image, and badge flow', async () => {
    expect((await client.get('/products/10/real-price?sellerId=1')).status).toBe(200);
    expect((await client.post('/fraud/admin/products/10/scan', {}, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.get('/fraud/admin/products/10/flags', { headers: authHeaders('ADMIN') })).status).toBe(200);
    expect((await client.get('/fraud/alerts', { headers: authHeaders('ADMIN') })).status).toBe(200);
    expect((await client.patch('/fraud/alerts/1/approve', {}, { headers: authHeaders('ADMIN') })).status).toBe(200);
    expect((await client.get('/trust/sellers/1')).status).toBe(200);
    expect((await client.get('/trust/sellers/1/history')).status).toBe(200);
    expect((await client.post('/trust/admin/sellers/1/recalculate', {
      deliveryAccuracy: 95, priceAccuracy: 90, customerRating: 92, responseSpeed: 88, returnRate: 5,
    }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.post('/i18n/admin/translations', { locale: 'en', namespace: 'product', key: 'product.lowest_price', value: 'Lowest Price' }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.get('/i18n/translations?locale=en&namespace=product')).status).toBe(200);
    expect((await client.post('/i18n/admin/exchange-rates', { baseCurrency: 'KRW', targetCurrency: 'USD', rate: 0.000748 }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.get('/i18n/exchange-rates')).status).toBe(200);
    expect((await client.get('/i18n/convert?amount=1590000&from=KRW&to=USD')).status).toBe(200);
    expect((await client.get('/images/1/variants')).status).toBe(200);
    expect((await client.delete('/images/1', { headers: authHeaders('ADMIN') })).status).toBe(200);
    expect((await client.post('/admin/badges', {
      name: '리뷰 마스터',
      description: '리뷰 10개 이상 작성',
      iconUrl: 'https://example.com/badges/review-master.svg',
      type: 'AUTO',
      condition: { metric: 'review_count', threshold: 10 },
      rarity: 'COMMON',
    }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.get('/badges')).status).toBe(200);
    expect((await client.post('/admin/badges/1/grant', { userId: 2 }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.get('/badges/me', { headers: authHeaders('USER') })).status).toBe(200);
  });
});
