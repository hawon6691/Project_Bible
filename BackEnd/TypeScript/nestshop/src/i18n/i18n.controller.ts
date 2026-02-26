import { Body, Controller, Delete, Get, Param, ParseIntPipe, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { ConvertAmountQueryDto } from './dto/convert-amount-query.dto';
import { TranslationQueryDto } from './dto/translation-query.dto';
import { UpsertExchangeRateDto } from './dto/upsert-exchange-rate.dto';
import { UpsertTranslationDto } from './dto/upsert-translation.dto';
import { I18nService } from './i18n.service';

@ApiTags('I18n')
@Controller('i18n')
export class I18nController {
  constructor(private readonly i18nService: I18nService) {}

  @Public()
  @Get('translations')
  @ApiOperation({ summary: '번역 조회' })
  getTranslations(@Query() query: TranslationQueryDto) {
    return this.i18nService.getTranslations(query);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post('admin/translations')
  @ApiOperation({ summary: '번역 등록/수정 (Admin)' })
  upsertTranslation(@Body() dto: UpsertTranslationDto) {
    return this.i18nService.upsertTranslation(dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Delete('admin/translations/:id')
  @ApiOperation({ summary: '번역 삭제 (Admin)' })
  deleteTranslation(@Param('id', ParseIntPipe) id: number) {
    return this.i18nService.deleteTranslation(id);
  }

  @Public()
  @Get('exchange-rates')
  @ApiOperation({ summary: '환율 목록 조회' })
  getExchangeRates() {
    return this.i18nService.getExchangeRates();
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post('admin/exchange-rates')
  @ApiOperation({ summary: '환율 등록/수정 (Admin)' })
  upsertExchangeRate(@Body() dto: UpsertExchangeRateDto) {
    return this.i18nService.upsertExchangeRate(dto);
  }

  @Public()
  @Get('convert')
  @ApiOperation({ summary: '금액 환산' })
  convertAmount(@Query() query: ConvertAmountQueryDto) {
    return this.i18nService.convertAmount(query);
  }
}
