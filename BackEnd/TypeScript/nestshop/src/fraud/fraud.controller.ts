import { Controller, Get, Param, ParseIntPipe, Patch, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { FraudAlertQueryDto } from './dto/fraud-alert-query.dto';
import { FraudScanQueryDto } from './dto/fraud-scan-query.dto';
import { RealPriceQueryDto } from './dto/real-price-query.dto';
import { FraudService } from './fraud.service';

@ApiTags('Fraud')
@Controller()
export class FraudController {
  constructor(private readonly fraudService: FraudService) {}

  // FRAUD-39-01: 이상 가격 알림 목록
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Get('fraud/alerts')
  @ApiOperation({ summary: '이상 가격 알림 목록 조회 (Admin)' })
  getAlerts(@Query() query: FraudAlertQueryDto) {
    return this.fraudService.getAlerts(query);
  }

  // FRAUD-39-02: 이상 가격 알림 승인
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Patch('fraud/alerts/:id/approve')
  @ApiOperation({ summary: '이상 가격 알림 승인 (Admin)' })
  approveAlert(@Param('id', ParseIntPipe) id: number, @CurrentUser() user: JwtPayload) {
    return this.fraudService.approveAlert(id, user.sub);
  }

  // FRAUD-39-03: 이상 가격 알림 거절
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Patch('fraud/alerts/:id/reject')
  @ApiOperation({ summary: '이상 가격 알림 거절 (Admin)' })
  rejectAlert(@Param('id', ParseIntPipe) id: number, @CurrentUser() user: JwtPayload) {
    return this.fraudService.rejectAlert(id, user.sub);
  }

  // FRAUD-39-04: 배송비 포함 실제 가격
  @Public()
  @Get('products/:id/real-price')
  @ApiOperation({ summary: '배송비 포함 실제 가격 조회' })
  getRealPrice(@Param('id', ParseIntPipe) productId: number, @Query() query: RealPriceQueryDto) {
    return this.fraudService.getRealPrice(productId, query);
  }

  // 이전 단계 API 유지: 배송비 포함 체감가 조회
  @Public()
  @Get('fraud/products/:productId/effective-prices')
  @ApiOperation({ summary: '배송비 포함 체감가 조회' })
  getEffectivePrices(@Param('productId', ParseIntPipe) productId: number) {
    return this.fraudService.getEffectivePrices(productId);
  }

  // 이전 단계 API 유지: 이상 가격 탐지 (실시간 계산)
  @Public()
  @Get('fraud/products/:productId/anomalies')
  @ApiOperation({ summary: '상품 이상 가격 탐지 결과 조회' })
  detectAnomalies(
    @Param('productId', ParseIntPipe) productId: number,
    @Query() query: FraudScanQueryDto,
  ) {
    return this.fraudService.detectAnomalies(productId, query, false);
  }

  // 이전 단계 API 유지: 이상 가격 탐지 결과 저장
  @ApiBearerAuth()
  @Post('fraud/admin/products/:productId/scan')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '이상 가격 탐지 실행 및 결과 저장 (Admin)' })
  scanAndPersist(
    @Param('productId', ParseIntPipe) productId: number,
    @Query() query: FraudScanQueryDto,
  ) {
    return this.fraudService.detectAnomalies(productId, query, true);
  }

  // 이전 단계 API 유지: 탐지 로그 조회
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Get('fraud/admin/products/:productId/flags')
  @ApiOperation({ summary: '저장된 이상 가격 탐지 로그 조회 (Admin)' })
  getFlags(@Param('productId', ParseIntPipe) productId: number) {
    return this.fraudService.getFlags(productId);
  }
}
