import { Controller, Get, Param, ParseIntPipe, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { UsedMarketCategoryQueryDto } from './dto/used-market-category-query.dto';
import { UsedMarketService } from './used-market.service';

@ApiTags('UsedMarket')
@Controller('used-market')
export class UsedMarketController {
  constructor(private readonly usedMarketService: UsedMarketService) {}

  @Public()
  @Get('products/:id/price')
  @ApiOperation({ summary: '특정 상품 중고 시세 조회' })
  getProductUsedPrice(@Param('id', ParseIntPipe) productId: number) {
    return this.usedMarketService.getProductUsedPrice(productId);
  }

  @Public()
  @Get('categories/:id/prices')
  @ApiOperation({ summary: '카테고리별 중고 시세 목록 조회' })
  getCategoryUsedPrices(
    @Param('id', ParseIntPipe) categoryId: number,
    @Query() query: UsedMarketCategoryQueryDto,
  ) {
    return this.usedMarketService.getCategoryUsedPrices(categoryId, query);
  }

  @ApiBearerAuth()
  @Post('pc-builds/:buildId/estimate')
  @ApiOperation({ summary: 'PC 견적 기반 중고 매입가 산정' })
  estimatePcBuildUsedPrice(
    @CurrentUser() user: JwtPayload,
    @Param('buildId', ParseIntPipe) buildId: number,
  ) {
    return this.usedMarketService.estimatePcBuildUsedPrice(user.sub, buildId);
  }
}
