import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  HttpStatus,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  Put,
  Query,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { CompareSpecsDto, ScoredCompareDto, SetSpecScoresDto } from './dto/compare-specs.dto';
import { CreateSpecDefinitionDto } from './dto/create-spec-definition.dto';
import { SetProductSpecsDto } from './dto/set-product-specs.dto';
import { NumericCompareDto, ScoreByCategoryDto, SimilarProductsQueryDto } from './dto/spec-engine.dto';
import { UpdateSpecDefinitionDto } from './dto/update-spec-definition.dto';
import { SpecService } from './spec.service';

@ApiTags('Specs')
@Controller('specs')
export class SpecController {
  constructor(private readonly specService: SpecService) {}

  @Public()
  @Get('definitions')
  @ApiOperation({ summary: '카테고리별 스펙 정의 목록' })
  findDefinitions(@Query('categoryId') categoryId?: number) {
    return this.specService.findDefinitions(categoryId);
  }

  // SENG-01: 상위 카테고리 스키마를 포함한 동적 스펙 정의 병합 조회
  @Public()
  @Get('definitions/resolved/:categoryId')
  @ApiOperation({ summary: '카테고리 상속 스펙 정의 조회' })
  getResolvedDefinitions(@Param('categoryId', ParseIntPipe) categoryId: number) {
    return this.specService.getResolvedDefinitions(categoryId);
  }

  @Post('definitions')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '스펙 정의 생성 (Admin)' })
  createDefinition(@Body() dto: CreateSpecDefinitionDto) {
    return this.specService.createDefinition(dto);
  }

  @Patch('definitions/:id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '스펙 정의 수정 (Admin)' })
  updateDefinition(@Param('id', ParseIntPipe) id: number, @Body() dto: UpdateSpecDefinitionDto) {
    return this.specService.updateDefinition(id, dto);
  }

  @Delete('definitions/:id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '스펙 정의 삭제 (Admin)' })
  removeDefinition(@Param('id', ParseIntPipe) id: number) {
    return this.specService.removeDefinition(id);
  }

  @Public()
  @Post('compare')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '상품 스펙 비교 (2~4개)' })
  compareSpecs(@Body() dto: CompareSpecsDto) {
    return this.specService.compareSpecs(dto);
  }

  // SENG-03: 수치형 스펙 자동 하이라이트 비교
  @Public()
  @Post('compare/numeric')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '수치형 스펙 비교 (best/worst 하이라이트)' })
  numericCompare(@Body() dto: NumericCompareDto) {
    return this.specService.numericCompare(dto);
  }

  @Public()
  @Post('compare/scored')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '점수화 스펙 비교 (가중치 적용)' })
  scoredCompare(@Body() dto: ScoredCompareDto) {
    return this.specService.scoredCompare(dto);
  }

  // SENG-04: 카테고리 기준 종합 점수 산출
  @Public()
  @Post('score')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '카테고리 기준 종합 성능 점수 계산' })
  scoreByCategory(@Body() dto: ScoreByCategoryDto) {
    return this.specService.scoreByCategory(dto);
  }

  @Put('scores/:specDefId')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '스펙 점수 매핑 설정 (Admin)' })
  setSpecScores(@Param('specDefId', ParseIntPipe) specDefId: number, @Body() dto: SetSpecScoresDto) {
    return this.specService.setSpecScores(specDefId, dto);
  }
}

@ApiTags('Products')
@Controller('products')
export class ProductSpecController {
  constructor(private readonly specService: SpecService) {}

  @Public()
  @Get(':id/specs')
  @ApiOperation({ summary: '상품 스펙 조회' })
  getProductSpecs(@Param('id', ParseIntPipe) id: number) {
    return this.specService.getProductSpecs(id);
  }

  // SENG-02: 스펙 그룹 단위 조회
  @Public()
  @Get(':id/specs/grouped')
  @ApiOperation({ summary: '상품 스펙 그룹핑 조회' })
  getGroupedSpecs(@Param('id', ParseIntPipe) id: number) {
    return this.specService.getGroupedSpecs(id);
  }

  // SENG-05: 유사도 기반 대안 상품 추천
  @Public()
  @Get(':id/similar-spec-products')
  @ApiOperation({ summary: '스펙 유사 상품 추천' })
  getSimilarProducts(
    @Param('id', ParseIntPipe) id: number,
    @Query() query: SimilarProductsQueryDto,
  ) {
    return this.specService.findSimilarProducts(id, query.limit);
  }

  @Put(':id/specs')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 스펙 값 설정 (Admin)' })
  setProductSpecs(@Param('id', ParseIntPipe) id: number, @Body() dto: SetProductSpecsDto) {
    return this.specService.setProductSpecs(id, dto);
  }
}
