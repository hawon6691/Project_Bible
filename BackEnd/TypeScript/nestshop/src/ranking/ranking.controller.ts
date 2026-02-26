import { Controller, Get, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { RankingQueryDto } from './dto/ranking-query.dto';
import { RankingService } from './ranking.service';

@ApiTags('Ranking')
@Controller('rankings')
export class RankingController {
  constructor(private readonly rankingService: RankingService) {}

  // 인기 상품 랭킹
  @Public()
  @Get('products/popular')
  @ApiOperation({ summary: '인기 상품 랭킹 조회' })
  getPopularProducts(@Query() query: RankingQueryDto) {
    return this.rankingService.getPopularProducts(query);
  }

  // 인기 검색어 랭킹
  @Public()
  @Get('keywords/popular')
  @ApiOperation({ summary: '인기 검색어 랭킹 조회' })
  getPopularKeywords(@Query() query: RankingQueryDto) {
    return this.rankingService.getPopularKeywords(query);
  }

  // 인기 점수 재계산 (Admin)
  @ApiBearerAuth()
  @Post('admin/recalculate')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '인기 점수 재계산 (Admin)' })
  recalculatePopularityScore() {
    return this.rankingService.recalculatePopularityScore();
  }
}
