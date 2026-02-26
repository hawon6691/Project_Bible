import { Body, Controller, Get, Param, ParseIntPipe, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { RecalculateTrustScoreDto } from './dto/recalculate-trust-score.dto';
import { TrustHistoryQueryDto } from './dto/trust-history-query.dto';
import { TrustService } from './trust.service';

@ApiTags('Trust')
@Controller('trust')
export class TrustController {
  constructor(private readonly trustService: TrustService) {}

  @Public()
  @Get('sellers/:sellerId')
  @ApiOperation({ summary: '판매처 현재 신뢰도 조회' })
  getCurrentScore(@Param('sellerId', ParseIntPipe) sellerId: number) {
    return this.trustService.getCurrentScore(sellerId);
  }

  @Public()
  @Get('sellers/:sellerId/history')
  @ApiOperation({ summary: '판매처 신뢰도 이력 조회' })
  getHistory(@Param('sellerId', ParseIntPipe) sellerId: number, @Query() query: TrustHistoryQueryDto) {
    return this.trustService.getHistory(sellerId, query);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post('admin/sellers/:sellerId/recalculate')
  @ApiOperation({ summary: '판매처 신뢰도 재산정 (Admin)' })
  recalculateScore(
    @Param('sellerId', ParseIntPipe) sellerId: number,
    @Body() dto: RecalculateTrustScoreDto,
  ) {
    return this.trustService.recalculateScore(sellerId, dto);
  }
}
