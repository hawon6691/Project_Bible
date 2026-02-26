import { INestApplication } from '@nestjs/common';
import { AuthController } from '../../src/auth/auth.controller';
import { AuthService } from '../../src/auth/auth.service';
import { SearchController } from '../../src/search/search.controller';
import { SearchService } from '../../src/search/search.service';
import { createE2eApp } from './test-app.factory';
import { startTestServer } from './http-client';

describe('Auth & Search E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;

  const authServiceMock = {
    signup: jest.fn().mockImplementation(async (dto: { email: string; name: string; phone: string }) => ({
      user: {
        id: 1,
        email: dto.email,
        name: dto.name,
        phone: dto.phone,
      },
      emailVerificationRequired: true,
    })),
  };

  const searchServiceMock = {
    search: jest.fn().mockResolvedValue({
      items: [{ id: 10, name: '테스트 상품', lowestPrice: 10000 }],
      meta: {
        page: 1,
        limit: 20,
        totalItems: 1,
        totalPages: 1,
      },
      relatedKeywords: ['테스트'],
      engine: 'database',
    }),
    autocomplete: jest.fn().mockResolvedValue({
      items: [{ keyword: '테스트 상품', highlighted: '<em>테스트</em> 상품' }],
      engine: 'database',
    }),
    getPopularKeywords: jest.fn().mockResolvedValue({
      items: [{ rank: 1, keyword: '테스트', count: 10 }],
    }),
  };

  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [AuthController, SearchController],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: SearchService, useValue: searchServiceMock },
      ],
    });
    client = await startTestServer(app);
  });

  afterAll(async () => {
    await app.close();
  });

  it('POST /auth/signup should validate payload', async () => {
    const res = await client.post('/auth/signup', {
        email: 'not-an-email',
        password: '1234',
        name: 'A',
        phone: '0101234',
      });
    expect(res.status).toBe(400);

    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('COMMON_001');
  });

  it('POST /auth/signup should return wrapped response on success', async () => {
    const res = await client.post('/auth/signup', {
        email: 'tester@example.com',
        password: 'Password1!',
        name: '테스터',
        phone: '010-1234-5678',
      });
    expect(res.status).toBe(201);

    expect(res.body.success).toBe(true);
    expect(res.body.data.user.email).toBe('tester@example.com');
    expect(authServiceMock.signup).toHaveBeenCalledTimes(1);
  });

  it('GET /search/autocomplete should validate query', async () => {
    const res = await client.get('/search/autocomplete?q=test&limit=0');
    expect(res.status).toBe(400);

    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('COMMON_001');
  });

  it('GET /search should return wrapped search result', async () => {
    const res = await client.get('/search?keyword=%ED%85%8C%EC%8A%A4%ED%8A%B8');
    expect(res.status).toBe(200);

    expect(res.body.success).toBe(true);
    expect(Array.isArray(res.body.data)).toBe(true);
    expect(res.body.meta.totalCount).toBe(1);
    expect(searchServiceMock.search).toHaveBeenCalledTimes(1);
  });
});
