import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { DealController } from '../../src/deal/deal.controller';
import { DealService } from '../../src/deal/deal.service';
import { PredictionController } from '../../src/prediction/prediction.controller';
import { PredictionService } from '../../src/prediction/prediction.service';
import { RecommendationController } from '../../src/recommendation/recommendation.controller';
import { RecommendationService } from '../../src/recommendation/recommendation.service';
import { RankingController } from '../../src/ranking/ranking.controller';
import { RankingService } from '../../src/ranking/ranking.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Prediction Deal Recommendation Ranking API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;
  const predictionServiceMock = { predictProductPrice: jest.fn().mockResolvedValue({ productName: 'PB UltraBook' }) };
  const dealServiceMock = {
    findDeals: jest.fn().mockResolvedValue([{ title: '봄맞이 특가' }]),
    create: jest.fn().mockResolvedValue({ id: 1, title: '봄맞이 특가' }),
    update: jest.fn().mockResolvedValue({ id: 1, stock: 15 }),
    remove: jest.fn().mockResolvedValue({ message: '특가가 삭제되었습니다.' }),
  };
  const recommendationServiceMock = {
    getTrendingRecommendations: jest.fn().mockResolvedValue([{ product: { name: 'PB Monitor' } }]),
    getPersonalRecommendations: jest.fn().mockResolvedValue([{ product: { name: 'PB Keyboard' } }]),
  };
  const rankingServiceMock = {
    getPopularProducts: jest.fn().mockResolvedValue([{ product: { name: 'PB Monitor' } }]),
    getPopularKeywords: jest.fn().mockResolvedValue([{ keyword: '모니터' }]),
    recalculatePopularityScore: jest.fn().mockResolvedValue({ updatedCount: 2 }),
  };
  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [PredictionController, DealController, RecommendationController, RankingController],
      providers: [
        { provide: PredictionService, useValue: predictionServiceMock },
        { provide: DealService, useValue: dealServiceMock },
        { provide: RecommendationService, useValue: recommendationServiceMock },
        { provide: RankingService, useValue: rankingServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });
  afterAll(async () => app.close());

  it('should cover prediction and deal flow', async () => {
    expect((await client.get('/predictions/products/10/price-trend?horizonDays=7&lookbackDays=30')).status).toBe(200);
    expect((await client.get('/deals')).status).toBe(200);
  });

  it('should cover recommendation and ranking flow', async () => {
    expect((await client.get('/recommendations/trending?limit=10')).status).toBe(200);
    expect((await client.get('/recommendations/personal?limit=10', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.get('/rankings/products/popular?limit=10')).status).toBe(200);
    expect((await client.get('/rankings/keywords/popular?limit=10')).status).toBe(200);
    expect((await client.post('/rankings/admin/recalculate', {}, { headers: authHeaders('ADMIN') })).status).toBe(201);
  });
});
