import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { FriendController } from '../../src/friend/friend.controller';
import { FriendService } from '../../src/friend/friend.service';
import { MatchingController } from '../../src/matching/matching.controller';
import { MatchingService } from '../../src/matching/matching.service';
import { MediaController } from '../../src/media/media.controller';
import { MediaService } from '../../src/media/media.service';
import { NewsController } from '../../src/news/news.controller';
import { NewsService } from '../../src/news/news.service';
import { PcBuilderController } from '../../src/pc-builder/pc-builder.controller';
import { PcBuilderService } from '../../src/pc-builder/pc-builder.service';
import { VideoController } from '../../src/video/video.controller';
import { VideoService } from '../../src/video/video.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Pc Friend Shortform Media News Matching API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;
  const pcBuilderServiceMock = {
    createBuild: jest.fn().mockResolvedValue({ id: 1, name: '내 게이밍 PC' }),
    addPart: jest.fn().mockResolvedValue({ parts: [{ product: { name: 'PB CPU' } }] }),
    createShareLink: jest.fn().mockResolvedValue({ shareCode: 'share-123' }),
    getSharedBuild: jest.fn().mockResolvedValue({ name: '내 게이밍 PC' }),
    createCompatibilityRule: jest.fn().mockResolvedValue({ id: 1 }),
  };
  const friendServiceMock = {
    requestFriend: jest.fn().mockResolvedValue({ id: 1 }),
    acceptRequest: jest.fn().mockResolvedValue({ accepted: true }),
    getFriends: jest.fn().mockResolvedValue({ items: [{ friend: { id: 2 } }], meta: { totalItems: 1 } }),
  };
  const videoServiceMock = {
    getFeed: jest.fn().mockResolvedValue([{ id: 1, title: '언박싱 숏폼' }]),
    getShortformDetail: jest.fn().mockResolvedValue({ id: 1, title: '언박싱 숏폼' }),
    toggleLike: jest.fn().mockResolvedValue({ liked: true }),
    createComment: jest.fn().mockResolvedValue({ id: 1, content: '좋은 영상입니다.' }),
    getComments: jest.fn().mockResolvedValue({ items: [{ content: '좋은 영상입니다.' }], meta: { totalItems: 1 } }),
  };
  const mediaServiceMock = {
    createPresignedUrl: jest.fn().mockResolvedValue({ url: 'https://example.com/upload' }),
    getMetadata: jest.fn().mockResolvedValue({ mime: 'application/pdf' }),
  };
  const newsServiceMock = {
    createCategory: jest.fn().mockResolvedValue({ id: 1, name: '리뷰' }),
    createNews: jest.fn().mockResolvedValue({ id: 1, products: [{ name: 'PB Camera' }] }),
    getNewsDetail: jest.fn().mockResolvedValue({ id: 1, products: [{ name: 'PB Camera' }] }),
  };
  const matchingServiceMock = {
    getPendingList: jest.fn().mockResolvedValue({ items: [{ id: 1 }], meta: { totalItems: 1 } }),
    approveMapping: jest.fn().mockResolvedValue({ approved: true }),
    getStats: jest.fn().mockResolvedValue({ approved: 1 }),
  };
  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [PcBuilderController, FriendController, VideoController, MediaController, NewsController, MatchingController],
      providers: [
        { provide: PcBuilderService, useValue: pcBuilderServiceMock },
        { provide: FriendService, useValue: friendServiceMock },
        { provide: VideoService, useValue: videoServiceMock },
        { provide: MediaService, useValue: mediaServiceMock },
        { provide: NewsService, useValue: newsServiceMock },
        { provide: MatchingService, useValue: matchingServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });
  afterAll(async () => app.close());

  it('should cover pc builder and friend flow', async () => {
    expect((await client.post('/pc-builds', { name: '내 게이밍 PC', description: '테스트 견적', purpose: 'GAMING', budget: 2000000 }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.post('/pc-builds/1/parts', { partType: 'CPU', productId: 10, quantity: 1 }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/pc-builds/1/share', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.get('/pc-builds/shared/share-123')).status).toBe(200);
    expect((await client.post('/admin/compatibility-rules', { partType: 'CPU', targetPartType: 'MOTHERBOARD', title: '기본 CPU 규칙', description: 'CPU와 메인보드 호환성 규칙', severity: 'MEDIUM' }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.post('/friends/request/2', {}, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.patch('/friends/request/1/accept', {}, { headers: authHeaders('USER', { 'x-user-id': '2' }) })).status).toBe(200);
    expect((await client.get('/friends?page=1&limit=10', { headers: authHeaders('USER') })).status).toBe(200);
  });

  it('should cover shortform, media, news, and matching flow', async () => {
    expect((await client.get('/shortforms?page=1&limit=10')).status).toBe(200);
    expect((await client.get('/shortforms/1')).status).toBe(200);
    expect((await client.post('/shortforms/1/like', {}, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.post('/shortforms/1/comments', { content: '좋은 영상입니다.' }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/shortforms/1/comments?page=1&limit=10')).status).toBe(200);
    expect((await client.post('/media/presigned-url', { fileName: 'manual.pdf', fileType: 'application/pdf', fileSize: 1024 }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/media/1/metadata')).status).toBe(200);
    expect((await client.post('/news/categories', { name: '리뷰', slug: 'review' }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.post('/news', { title: 'PB Camera 출시', content: '신제품 소식', categoryId: 1 }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.get('/news/1')).status).toBe(200);
    expect((await client.get('/matching/pending?page=1&limit=10', { headers: authHeaders('ADMIN') })).status).toBe(200);
    expect((await client.patch('/matching/1/approve', { productId: 10 }, { headers: authHeaders('ADMIN') })).status).toBe(200);
    expect((await client.get('/matching/stats', { headers: authHeaders('ADMIN') })).status).toBe(200);
  });
});
