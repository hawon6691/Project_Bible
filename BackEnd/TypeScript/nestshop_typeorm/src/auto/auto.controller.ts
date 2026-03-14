import { Body, Controller, Get, Param, ParseIntPipe, Post, Query } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { AutoService } from './auto.service';
import { AutoEstimateDto } from './dto/auto-estimate.dto';
import { CarModelQueryDto } from './dto/car-model-query.dto';

@ApiTags('Auto')
@Controller('auto')
export class AutoController {
  constructor(private readonly autoService: AutoService) {}

  @Public()
  @Get('models')
  @ApiOperation({ summary: '자동차 모델 목록 조회' })
  getModels(@Query() query: CarModelQueryDto) {
    return this.autoService.getModels(query);
  }

  @Public()
  @Get('models/:id/trims')
  @ApiOperation({ summary: '모델별 트림/옵션 조회' })
  getTrims(@Param('id', ParseIntPipe) modelId: number) {
    return this.autoService.getTrims(modelId);
  }

  @Public()
  @Post('estimate')
  @ApiOperation({ summary: '신차 견적 계산' })
  estimate(@Body() dto: AutoEstimateDto) {
    return this.autoService.estimate(dto);
  }

  @Public()
  @Get('models/:id/lease-offers')
  @ApiOperation({ summary: '모델별 렌트/리스 오퍼 조회' })
  getLeaseOffers(@Param('id', ParseIntPipe) modelId: number) {
    return this.autoService.getLeaseOffers(modelId);
  }
}
