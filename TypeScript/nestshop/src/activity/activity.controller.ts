import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Post,
  Query,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { PaginationRequestDto } from '../common/dto/pagination.dto';
import { CreateSearchHistoryDto } from './dto/create-search-history.dto';
import { ActivityService } from './activity.service';

@ApiTags('Activity')
@Controller('activities')
@ApiBearerAuth()
export class ActivityController {
  constructor(private readonly activityService: ActivityService) {}

  // ACT-01: 활동 내역 통합 조회
  @Get()
  @ApiOperation({ summary: '활동 내역 통합 조회' })
  getSummary(@CurrentUser() user: JwtPayload) {
    return this.activityService.getSummary(user.sub);
  }

  // ACT-02: 최근 본 상품 조회
  @Get('recent-products')
  @ApiOperation({ summary: '최근 본 상품 조회' })
  getRecentProducts(@CurrentUser() user: JwtPayload, @Query() query: PaginationRequestDto) {
    return this.activityService.getRecentProducts(user.sub, query);
  }

  // 최근 본 상품 기록 추가
  @Post('recent-products/:productId')
  @ApiOperation({ summary: '최근 본 상품 기록 추가' })
  addRecentProduct(@CurrentUser() user: JwtPayload, @Param('productId', ParseIntPipe) productId: number) {
    return this.activityService.addRecentProduct(user.sub, productId);
  }

  // ACT-03: 검색 기록 조회
  @Get('searches')
  @ApiOperation({ summary: '검색 기록 조회' })
  getSearchHistory(@CurrentUser() user: JwtPayload, @Query() query: PaginationRequestDto) {
    return this.activityService.getSearchHistory(user.sub, query);
  }

  // 검색 기록 추가
  @Post('searches')
  @ApiOperation({ summary: '검색 기록 추가' })
  addSearchHistory(@CurrentUser() user: JwtPayload, @Body() dto: CreateSearchHistoryDto) {
    return this.activityService.addSearchHistory(user.sub, dto);
  }

  // ACT-04: 검색 기록 삭제 (개별)
  @Delete('searches/:id')
  @ApiOperation({ summary: '검색 기록 개별 삭제' })
  removeSearchHistory(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.activityService.removeSearchHistory(user.sub, id);
  }

  // ACT-04: 검색 기록 삭제 (전체)
  @Delete('searches')
  @ApiOperation({ summary: '검색 기록 전체 삭제' })
  clearSearchHistory(@CurrentUser() user: JwtPayload) {
    return this.activityService.clearSearchHistory(user.sub);
  }
}
