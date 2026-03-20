import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { API_ROUTES } from '../routes/api-routes';
import { CreateCrawlerJobDto } from './dto/create-crawler-job.dto';
import { CrawlerJobQueryDto } from './dto/crawler-job-query.dto';
import { CrawlerRunQueryDto } from './dto/crawler-run-query.dto';
import { TriggerCrawlerDto } from './dto/trigger-crawler.dto';
import { UpdateCrawlerJobDto } from './dto/update-crawler-job.dto';
import { CrawlerService } from './crawler.service';

@ApiTags('Crawler')
@ApiBearerAuth()
@Roles(UserRole.ADMIN)
@Controller(API_ROUTES.CRAWLER.BASE)
export class CrawlerController {
  constructor(private readonly crawlerService: CrawlerService) {}

  @Get(API_ROUTES.CRAWLER.ADMIN_JOBS)
  @ApiOperation({ summary: '크롤러 작업 목록 조회 (Admin)' })
  getJobs(@Query() query: CrawlerJobQueryDto) {
    return this.crawlerService.getJobs(query);
  }

  @Post(API_ROUTES.CRAWLER.ADMIN_JOBS)
  @ApiOperation({ summary: '크롤러 작업 생성 (Admin)' })
  createJob(@Body() dto: CreateCrawlerJobDto) {
    return this.crawlerService.createJob(dto);
  }

  @Patch(API_ROUTES.CRAWLER.ADMIN_JOB_DETAIL)
  @ApiOperation({ summary: '크롤러 작업 수정 (Admin)' })
  updateJob(@Param('id', ParseIntPipe) id: number, @Body() dto: UpdateCrawlerJobDto) {
    return this.crawlerService.updateJob(id, dto);
  }

  @Delete(API_ROUTES.CRAWLER.ADMIN_JOB_DETAIL)
  @ApiOperation({ summary: '크롤러 작업 삭제 (Admin)' })
  removeJob(@Param('id', ParseIntPipe) id: number) {
    return this.crawlerService.removeJob(id);
  }

  @Post(API_ROUTES.CRAWLER.ADMIN_JOB_RUN)
  @ApiOperation({ summary: '작업 단위 수동 수집 실행 (Admin)' })
  triggerJob(@Param('id', ParseIntPipe) id: number) {
    return this.crawlerService.triggerJob(id);
  }

  @Post(API_ROUTES.CRAWLER.ADMIN_TRIGGERS)
  @ApiOperation({ summary: '특정 판매처/상품 즉시 수집 실행 (Admin)' })
  triggerManual(@Body() dto: TriggerCrawlerDto) {
    return this.crawlerService.triggerManual(dto);
  }

  @Get(API_ROUTES.CRAWLER.ADMIN_RUNS)
  @ApiOperation({ summary: '크롤러 실행 이력 조회 (Admin)' })
  getRuns(@Query() query: CrawlerRunQueryDto) {
    return this.crawlerService.getRuns(query);
  }

  @Get(API_ROUTES.CRAWLER.ADMIN_MONITORING)
  @ApiOperation({ summary: '수집 상태 모니터링 통계 조회 (Admin)' })
  getMonitoringSummary() {
    return this.crawlerService.getMonitoringSummary();
  }
}
