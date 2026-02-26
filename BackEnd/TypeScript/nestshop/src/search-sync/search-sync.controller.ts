import { Controller, Get, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { API_ROUTES } from '../routes/api-routes';
import { SearchSyncService } from './search-sync.service';

@ApiTags('SearchSync')
@ApiBearerAuth()
@Roles(UserRole.ADMIN)
@Controller(API_ROUTES.SEARCH.ADMIN_INDEX_OUTBOX_BASE)
export class SearchSyncController {
  constructor(private readonly searchSyncService: SearchSyncService) {}

  @Get(API_ROUTES.SEARCH.ADMIN_INDEX_OUTBOX_SUMMARY)
  @ApiOperation({ summary: '검색 인덱스 동기화 Outbox 요약 조회' })
  getSummary() {
    return this.searchSyncService.getOutboxSummary();
  }

  @Post(API_ROUTES.SEARCH.ADMIN_INDEX_OUTBOX_REQUEUE_FAILED)
  @ApiOperation({ summary: '검색 인덱스 동기화 실패 Outbox 재큐잉' })
  requeueFailed(@Query('limit') limit?: string) {
    const parsed = limit ? Number(limit) : undefined;
    return this.searchSyncService.requeueFailed(Number.isFinite(parsed) ? parsed : undefined);
  }
}
