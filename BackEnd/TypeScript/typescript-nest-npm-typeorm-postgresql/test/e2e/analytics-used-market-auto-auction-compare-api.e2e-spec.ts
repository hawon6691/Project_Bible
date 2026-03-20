import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { AnalyticsController } from '../../src/analytics/analytics.controller';
import { AnalyticsService } from '../../src/analytics/analytics.service';
import { AuctionController } from '../../src/auction/auction.controller';
import { AuctionService } from '../../src/auction/auction.service';
import { AutoController } from '../../src/auto/auto.controller';
import { AutoService } from '../../src/auto/auto.service';
import { CompareController } from '../../src/compare/compare.controller';
import { CompareService } from '../../src/compare/compare.service';
import { UsedMarketController } from '../../src/used-market/used-market.controller';
import { UsedMarketService } from '../../src/used-market/used-market.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Analytics Used Market Auto Auction Compare API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;
  const analyticsServiceMock = {
    getLowestEver: jest.fn().mockResolvedValue({ lowestPrice: 140000 }),
    getUnitPrice: jest.fn().mockResolvedValue({ unitPrice: 140 }),
  };
  const usedMarketServiceMock = {
    getProductUsedPrice: jest.fn().mockResolvedValue({ averagePrice: 92500 }),
    estimatePcBuildUsedPrice: jest.fn().mockResolvedValue({ estimatedPrice: 100000, partBreakdown: [] }),
  };
  const autoServiceMock = {
    getModels: jest.fn().mockResolvedValue([{ id: 1, name: 'PB E-Car' }]),
    estimate: jest.fn().mockResolvedValue({ optionPrice: 1200000 }),
  };
  const auctionServiceMock = {
    createAuction: jest.fn().mockResolvedValue({ id: 1 }),
    getAuctionDetail: jest.fn().mockResolvedValue({ id: 1, status: 'CLOSED' }),
    createBid: jest.fn().mockResolvedValue({ id: 1 }),
    selectBid: jest.fn().mockResolvedValue({ message: '낙찰을 선택했습니다.' }),
  };
  const compareServiceMock = {
    add: jest.fn().mockResolvedValue({ added: true }),
    getList: jest.fn().mockResolvedValue({ compareList: [{ productId: 10 }, { productId: 11 }] }),
    getDetail: jest.fn().mockResolvedValue({ items: [{ productId: 10 }, { productId: 11 }] }),
  };
  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [AnalyticsController, UsedMarketController, AutoController, AuctionController, CompareController],
      providers: [
        { provide: AnalyticsService, useValue: analyticsServiceMock },
        { provide: UsedMarketService, useValue: usedMarketServiceMock },
        { provide: AutoService, useValue: autoServiceMock },
        { provide: AuctionService, useValue: auctionServiceMock },
        { provide: CompareService, useValue: compareServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });
  afterAll(async () => app.close());

  it('should cover analytics, used market, and auto flow', async () => {
    expect((await client.get('/analytics/products/10/lowest-ever')).status).toBe(200);
    expect((await client.get('/used-market/products/10/price')).status).toBe(200);
    expect((await client.post('/used-market/pc-builds/1/estimate', {}, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/auto/models')).status).toBe(200);
    expect((await client.post('/auto/estimate', { modelId: 1, trimId: 1, optionIds: [1] })).status).toBe(201);
  });

  it('should cover auction and compare flow', async () => {
    expect((await client.post('/auctions', { title: '노트북 구매 요청', description: '가성비 견적', categoryId: 1, budget: 1500000 }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.post('/auctions/1/bids', { price: 1420000, description: '3일 내 배송', deliveryDays: 3 }, { headers: authHeaders('SELLER') })).status).toBe(201);
    expect((await client.patch('/auctions/1/bids/1/select', {}, { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.get('/auctions/1')).status).toBe(200);
    expect((await client.post('/compare/add', { productId: 10 }, { headers: { 'x-compare-key': 'test-compare' } })).status).toBe(201);
    expect((await client.get('/compare', { headers: { 'x-compare-key': 'test-compare' } })).status).toBe(200);
    expect((await client.get('/compare/detail', { headers: { 'x-compare-key': 'test-compare' } })).status).toBe(200);
  });
});
