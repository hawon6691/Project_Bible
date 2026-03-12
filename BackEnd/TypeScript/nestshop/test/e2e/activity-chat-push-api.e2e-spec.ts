import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { ActivityController } from '../../src/activity/activity.controller';
import { ActivityService } from '../../src/activity/activity.service';
import { ChatController } from '../../src/chat/chat.controller';
import { ChatService } from '../../src/chat/chat.service';
import { PushController } from '../../src/push/push.controller';
import { PushService } from '../../src/push/push.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Activity Chat Push API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;
  const activityServiceMock = {
    enqueueRecentProduct: jest.fn().mockResolvedValue({ productName: 'PB Laptop Air' }),
    enqueueSearchHistory: jest.fn().mockResolvedValue({ id: 1, keyword: '게이밍 노트북' }),
    getSummary: jest.fn().mockResolvedValue({ recentProductCount: 1, searchCount: 1 }),
    getRecentProducts: jest.fn().mockResolvedValue({ items: [{ productName: 'PB Laptop Air' }], meta: { totalItems: 1 } }),
    getSearchHistory: jest.fn().mockResolvedValue({ items: [{ keyword: '게이밍 노트북' }], meta: { totalItems: 1 } }),
    removeSearchHistory: jest.fn().mockResolvedValue({ message: '검색 기록이 삭제되었습니다.' }),
    clearSearchHistory: jest.fn().mockResolvedValue({ message: '검색 기록이 전체 삭제되었습니다.' }),
  };
  const chatServiceMock = {
    createRoom: jest.fn().mockResolvedValue({ id: 3, name: '배송 문의 채팅', members: [{ userId: 1 }] }),
    joinRoom: jest.fn().mockResolvedValue({ joined: true }),
    sendMessage: jest.fn().mockResolvedValue({ id: 9, message: '안녕하세요. 문의드립니다.' }),
    findMyRooms: jest.fn().mockResolvedValue({ items: [{ name: '배송 문의 채팅' }], meta: { totalItems: 1 } }),
    findMessages: jest.fn().mockResolvedValue({ items: [{ message: '안녕하세요. 문의드립니다.' }], meta: { totalItems: 1 } }),
  };
  const pushServiceMock = {
    registerSubscription: jest.fn().mockResolvedValue({ isActive: true }),
    unregisterSubscription: jest.fn().mockResolvedValue({ message: '푸시 구독이 해제되었습니다.' }),
    getMySubscriptions: jest.fn().mockResolvedValue([{ endpoint: 'https://push.example.com/sub/1' }]),
    getPreference: jest.fn().mockResolvedValue({ priceAlertEnabled: true }),
    updatePreference: jest.fn().mockResolvedValue({ priceAlertEnabled: false, chatMessageEnabled: false }),
  };
  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [ActivityController, ChatController, PushController],
      providers: [
        { provide: ActivityService, useValue: activityServiceMock },
        { provide: ChatService, useValue: chatServiceMock },
        { provide: PushService, useValue: pushServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });
  afterAll(async () => app.close());

  it('should cover activity flow', async () => {
    expect((await client.post('/activities/recent-products/10', {}, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.post('/activities/searches', { keyword: '게이밍 노트북' }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/activities', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.get('/activities/recent-products?page=1&limit=10', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.get('/activities/searches?page=1&limit=10', { headers: authHeaders('USER') })).status).toBe(200);
  });

  it('should cover chat and push flow', async () => {
    expect((await client.post('/chat/rooms', { name: '배송 문의 채팅', isPrivate: true }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.post('/chat/rooms/3/join', {}, { headers: authHeaders('USER', { 'x-user-id': '2' }) })).status).toBe(201);
    expect((await client.post('/chat/rooms/3/messages', { message: '안녕하세요. 문의드립니다.' }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/chat/rooms?page=1&limit=10', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.get('/chat/rooms/3/messages?page=1&limit=10', { headers: authHeaders('USER') })).status).toBe(200);

    expect((await client.post('/push/subscriptions', {
      endpoint: 'https://push.example.com/sub/1', p256dhKey: 'p256dh-key', authKey: 'auth-key', expirationTime: '1741000000000',
    }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/push/subscriptions', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.get('/push/preferences', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.post('/push/preferences', { priceAlertEnabled: false, chatMessageEnabled: false }, { headers: authHeaders('USER') })).status).toBe(201);
  });
});
