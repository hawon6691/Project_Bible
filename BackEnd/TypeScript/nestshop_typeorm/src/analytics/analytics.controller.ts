import { Controller, Get, Param, ParseIntPipe } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { AnalyticsService } from './analytics.service';

@ApiTags('Analytics')
@Controller('analytics')
export class AnalyticsController {
  constructor(private readonly analyticsService: AnalyticsService) {}

  @Public()
  @Get('products/:id/lowest-ever')
  @ApiOperation({ summary: '역대 최저가 여부 확인' })
  getLowestEver(@Param('id', ParseIntPipe) productId: number) {
    return this.analyticsService.getLowestEver(productId);
  }

  @Public()
  @Get('products/:id/unit-price')
  @ApiOperation({ summary: '용량/수량당 단가 계산' })
  getUnitPrice(@Param('id', ParseIntPipe) productId: number) {
    return this.analyticsService.getUnitPrice(productId);
  }
}
