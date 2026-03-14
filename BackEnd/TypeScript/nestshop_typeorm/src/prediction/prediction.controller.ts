import { Controller, Get, Param, ParseIntPipe, Query } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { PricePredictionQueryDto } from './dto/price-prediction-query.dto';
import { PredictionService } from './prediction.service';

@ApiTags('Prediction')
@Controller('predictions')
export class PredictionController {
  constructor(private readonly predictionService: PredictionService) {}

  // PRICE-ANLY: 상품 가격 추세 예측
  @Public()
  @Get('products/:productId/price-trend')
  @ApiOperation({ summary: '상품 가격 추세 예측 조회' })
  predictProductPrice(
    @Param('productId', ParseIntPipe) productId: number,
    @Query() query: PricePredictionQueryDto,
  ) {
    return this.predictionService.predictProductPrice(productId, query);
  }
}
