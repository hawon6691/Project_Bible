import { Controller, Get, Param, ParseIntPipe, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { API_ROUTES } from '../routes/api-routes';
import { ProductQueryViewDto } from './dto/product-query-view.dto';
import { QueryService } from './query.service';

@ApiTags('Query')
@Controller()
export class QueryController {
  constructor(private readonly queryService: QueryService) {}

  // CQRS 읽기 모델 조회 API
  @Public()
  @Get(API_ROUTES.QUERY.PRODUCTS)
  @ApiOperation({ summary: '읽기 모델 기반 상품 목록 조회' })
  findProducts(@Query() query: ProductQueryViewDto) {
    return this.queryService.findProducts(query);
  }

  @Public()
  @Get(API_ROUTES.QUERY.PRODUCT_DETAIL)
  @ApiOperation({ summary: '읽기 모델 기반 상품 상세 조회' })
  findProductDetail(@Param('productId', ParseIntPipe) productId: number) {
    return this.queryService.findProductDetail(productId);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post(API_ROUTES.QUERY.ADMIN_SYNC_PRODUCT)
  @ApiOperation({ summary: '상품 단건 읽기 모델 동기화 (Admin)' })
  syncProduct(@Param('productId', ParseIntPipe) productId: number) {
    return this.queryService.syncProduct(productId);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post(API_ROUTES.QUERY.ADMIN_REBUILD)
  @ApiOperation({ summary: '전체 읽기 모델 재구축 (Admin)' })
  rebuildAll() {
    return this.queryService.rebuildAll();
  }
}
