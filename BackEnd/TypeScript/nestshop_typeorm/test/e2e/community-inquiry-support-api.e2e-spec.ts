import { APP_GUARD } from '@nestjs/core';
import { INestApplication } from '@nestjs/common';
import { CommunityController } from '../../src/community/community.controller';
import { CommunityService } from '../../src/community/community.service';
import { InquiryController } from '../../src/inquiry/inquiry.controller';
import { InquiryService } from '../../src/inquiry/inquiry.service';
import { SupportController } from '../../src/support/support.controller';
import { SupportService } from '../../src/support/support.service';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { createE2eApp } from './test-app.factory';
import { authHeaders, HeaderAuthGuard } from './header-auth.guard';
import { startTestServer } from './http-client';

describe('Community Inquiry Support API E2E', () => {
  let app: INestApplication;
  let client: Awaited<ReturnType<typeof startTestServer>>;
  const communityServiceMock = {
    getBoards: jest.fn().mockResolvedValue([{ id: 1, slug: 'free-board' }]),
    findPosts: jest.fn().mockResolvedValue({ items: [{ id: 1, title: '첫 글입니다' }], meta: { totalItems: 1 } }),
    findOne: jest.fn().mockResolvedValue({ id: 1, title: '첫 글입니다', viewCount: 1 }),
    create: jest.fn().mockResolvedValue({ id: 1, title: '첫 글입니다' }),
    update: jest.fn().mockResolvedValue({ id: 1, title: '수정된 글' }),
    remove: jest.fn().mockResolvedValue({ message: '게시글이 삭제되었습니다.' }),
  };
  const inquiryServiceMock = {
    create: jest.fn().mockResolvedValue({ id: 11, title: '재입고 문의' }),
    findMine: jest.fn().mockResolvedValue([{ id: 11, title: '재입고 문의' }]),
    findByProduct: jest.fn().mockResolvedValue([{ id: 11, content: '비밀 문의입니다.' }]),
    answer: jest.fn().mockResolvedValue({ id: 11, answer: '다음 주 입고 예정입니다.' }),
    remove: jest.fn().mockResolvedValue({ message: '문의글이 삭제되었습니다.' }),
  };
  const supportServiceMock = {
    create: jest.fn().mockResolvedValue({ id: 21, ticketNumber: 'SUP-001', status: 'OPEN', title: '배송 문의' }),
    findMine: jest.fn().mockResolvedValue([{ id: 21, title: '배송 문의' }]),
    findMyOne: jest.fn().mockResolvedValue({ id: 21, status: 'RESOLVED' }),
    answer: jest.fn().mockResolvedValue({ id: 21, status: 'ANSWERED', replies: [{ content: '오늘 중으로 확인 후 안내드리겠습니다.' }] }),
    findAll: jest.fn().mockResolvedValue([{ id: 21, ticketNumber: 'SUP-001' }]),
  };
  beforeAll(async () => {
    app = await createE2eApp({
      controllers: [CommunityController, InquiryController, SupportController],
      providers: [
        { provide: CommunityService, useValue: communityServiceMock },
        { provide: InquiryService, useValue: inquiryServiceMock },
        { provide: SupportService, useValue: supportServiceMock },
        { provide: APP_GUARD, useClass: HeaderAuthGuard },
        { provide: APP_GUARD, useClass: RolesGuard },
      ],
    });
    client = await startTestServer(app);
  });
  afterAll(async () => app.close());

  it('should manage community posts', async () => {
    expect((await client.get('/community/boards')).status).toBe(200);
    expect((await client.post('/community/posts', { boardType: 'FREE', title: '첫 글입니다', content: '커뮤니티 테스트 본문' }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/community/posts?page=1&limit=10')).status).toBe(200);
    expect((await client.get('/community/posts/1')).status).toBe(200);
    expect((await client.patch('/community/posts/1', { title: '수정된 글' }, { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.delete('/community/posts/1', { headers: authHeaders('USER') })).status).toBe(200);
  });

  it('should manage inquiry and support flows', async () => {
    expect((await client.post('/products/10/inquiries', { title: '재입고 문의', content: '언제 재입고되나요?', isSecret: true }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/inquiries/me?page=1&limit=10', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.get('/products/10/inquiries?page=1&limit=10')).status).toBe(200);
    expect((await client.post('/inquiries/11/answer', { content: '다음 주 입고 예정입니다.' }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.delete('/inquiries/11', { headers: authHeaders('USER') })).status).toBe(200);

    expect((await client.post('/support/tickets', { category: 'delivery', title: '배송 문의', content: '배송이 지연되고 있습니다.' }, { headers: authHeaders('USER') })).status).toBe(201);
    expect((await client.get('/support/tickets/me?page=1&limit=10', { headers: authHeaders('USER') })).status).toBe(200);
    expect((await client.post('/admin/support/tickets/21/answer', { content: '오늘 중으로 확인 후 안내드리겠습니다.' }, { headers: authHeaders('ADMIN') })).status).toBe(201);
    expect((await client.get('/admin/support/tickets?page=1&limit=10', { headers: authHeaders('ADMIN') })).status).toBe(200);
  });
});
