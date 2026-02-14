import { Controller, Get, Param, ParseIntPipe, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { FraudScanQueryDto } from './dto/fraud-scan-query.dto';
import { FraudService } from './fraud.service';

@ApiTags('Fraud')
@Controller('fraud')
export class FraudController {
  constructor(private readonly fraudService: FraudService) {}

  // FRAUD-02: 배송비 포함 체감가 조회
  @Public()
  @Get('products/:productId/effective-prices')
  @ApiOperation({ summary: '배송비 포함 체감가 조회' })
  getEffectivePrices(@Param('productId', ParseIntPipe) productId: number) {
    return this.fraudService.getEffectivePrices(productId);
  }

  // FRAUD-01: 이상 가격 탐지 (실시간 계산)
  @Public()
  @Get('products/:productId/anomalies')
  @ApiOperation({ summary: '상품 이상 가격 탐지 결과 조회' })
  detectAnomalies(
    @Param('productId', ParseIntPipe) productId: number,
    @Query() query: FraudScanQueryDto,
  ) {
    return this.fraudService.detectAnomalies(productId, query, false);
  }

  // FRAUD-01: 이상 가격 탐지 결과 저장 (Admin)
  @ApiBearerAuth()
  @Post('admin/products/:productId/scan')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '이상 가격 탐지 실행 및 결과 저장 (Admin)' })
  scanAndPersist(
    @Param('productId', ParseIntPipe) productId: number,
    @Query() query: FraudScanQueryDto,
  ) {
    return this.fraudService.detectAnomalies(productId, query, true);
  }

  // 저장된 탐지 로그 조회
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Get('admin/products/:productId/flags')
  @ApiOperation({ summary: '저장된 이상 가격 탐지 로그 조회 (Admin)' })
  getFlags(@Param('productId', ParseIntPipe) productId: number) {
    return this.fraudService.getFlags(productId);
  }
}
