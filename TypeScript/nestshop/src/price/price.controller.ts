import {
  Controller, Get, Post, Patch, Delete,
  Body, Param, Query, HttpCode, HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { PriceService } from './price.service';
import { CreatePriceEntryDto, UpdatePriceEntryDto, CreatePriceAlertDto } from './dto/create-price-entry.dto';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { Public } from '../common/decorators/public.decorator';

@ApiTags('Prices')
@Controller()
export class PriceController {
  constructor(private readonly priceService: PriceService) {}

  // ─── PRICE-01: 판매처별 가격비교 ───
  @Public()
  @Get('products/:id/prices')
  @ApiOperation({ summary: '상품 판매처별 가격비교' })
  getProductPrices(@Param('id') id: number) {
    return this.priceService.getProductPrices(id);
  }

  // ─── SELL-04: 가격 등록 ───
  @Post('products/:id/prices')
  @Roles(UserRole.SELLER, UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '판매처 가격 등록 (Seller/Admin)' })
  createPriceEntry(@Param('id') id: number, @Body() dto: CreatePriceEntryDto) {
    return this.priceService.createPriceEntry(id, dto);
  }

  // ─── 가격 수정 ───
  @Patch('prices/:id')
  @Roles(UserRole.SELLER, UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '가격 수정 (Seller/Admin)' })
  updatePriceEntry(@Param('id') id: number, @Body() dto: UpdatePriceEntryDto) {
    return this.priceService.updatePriceEntry(id, dto);
  }

  // ─── 가격 삭제 ───
  @Delete('prices/:id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '가격 삭제 (Admin)' })
  removePriceEntry(@Param('id') id: number) {
    return this.priceService.removePriceEntry(id);
  }

  // ─── PRICE-02: 가격 추이 ───
  @Public()
  @Get('products/:id/price-history')
  @ApiOperation({ summary: '가격 추이 조회' })
  getPriceHistory(
    @Param('id') id: number,
    @Query('period') period?: string,
  ) {
    return this.priceService.getPriceHistory(id, period);
  }

  // ─── PRICE-04: 내 알림 목록 ───
  @Get('price-alerts')
  @ApiBearerAuth()
  @ApiOperation({ summary: '내 최저가 알림 목록' })
  getMyAlerts(@CurrentUser() user: JwtPayload) {
    return this.priceService.getMyAlerts(user.sub);
  }

  // ─── PRICE-03: 최저가 알림 등록 ───
  @Post('price-alerts')
  @ApiBearerAuth()
  @ApiOperation({ summary: '최저가 알림 등록' })
  createAlert(@CurrentUser() user: JwtPayload, @Body() dto: CreatePriceAlertDto) {
    return this.priceService.createAlert(user.sub, dto);
  }

  // ─── PRICE-05: 알림 삭제 ───
  @Delete('price-alerts/:id')
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '최저가 알림 삭제' })
  removeAlert(@CurrentUser() user: JwtPayload, @Param('id') id: number) {
    return this.priceService.removeAlert(user.sub, id);
  }
}
