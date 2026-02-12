import {
  Controller,
  Get,
  Post,
  Patch,
  Put,
  Delete,
  Body,
  Param,
  Query,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { SpecService } from './spec.service';
import { CreateSpecDefinitionDto } from './dto/create-spec-definition.dto';
import { UpdateSpecDefinitionDto } from './dto/update-spec-definition.dto';
import { SetProductSpecsDto } from './dto/set-product-specs.dto';
import { CompareSpecsDto, ScoredCompareDto, SetSpecScoresDto } from './dto/compare-specs.dto';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { Public } from '../common/decorators/public.decorator';

@ApiTags('Specs')
@Controller('specs')
export class SpecController {
  constructor(private readonly specService: SpecService) {}

  // ─── SPEC-01: 스펙 정의 목록 조회 ───
  @Public()
  @Get('definitions')
  @ApiOperation({ summary: '카테고리별 스펙 정의 목록' })
  findDefinitions(@Query('categoryId') categoryId?: number) {
    return this.specService.findDefinitions(categoryId);
  }

  // ─── SPEC-01: 스펙 정의 생성 (Admin) ───
  @Post('definitions')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '스펙 정의 생성 (Admin)' })
  createDefinition(@Body() dto: CreateSpecDefinitionDto) {
    return this.specService.createDefinition(dto);
  }

  // ─── SPEC-01: 스펙 정의 수정 (Admin) ───
  @Patch('definitions/:id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '스펙 정의 수정 (Admin)' })
  updateDefinition(@Param('id') id: number, @Body() dto: UpdateSpecDefinitionDto) {
    return this.specService.updateDefinition(id, dto);
  }

  // ─── SPEC-01: 스펙 정의 삭제 (Admin) ───
  @Delete('definitions/:id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '스펙 정의 삭제 (Admin)' })
  removeDefinition(@Param('id') id: number) {
    return this.specService.removeDefinition(id);
  }

  // ─── SPEC-04: 상품 스펙 비교 ───
  @Public()
  @Post('compare')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '상품 스펙 비교 (2~4개)' })
  compareSpecs(@Body() dto: CompareSpecsDto) {
    return this.specService.compareSpecs(dto);
  }

  // ─── SPEC-04: 점수화 스펙 비교 ───
  @Public()
  @Post('compare/scored')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '점수화 스펙 비교 (가중치 적용)' })
  scoredCompare(@Body() dto: ScoredCompareDto) {
    return this.specService.scoredCompare(dto);
  }

  // ─── 스펙 점수 매핑 설정 (Admin) ───
  @Put('scores/:specDefId')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '스펙 점수 매핑 설정 (Admin)' })
  setSpecScores(@Param('specDefId') specDefId: number, @Body() dto: SetSpecScoresDto) {
    return this.specService.setSpecScores(specDefId, dto);
  }
}

// ─── 상품 스펙 라우트 (ProductController에서 사용) ───
@ApiTags('Products')
@Controller('products')
export class ProductSpecController {
  constructor(private readonly specService: SpecService) {}

  // ─── SPEC-02: 상품 스펙 조회 ───
  @Public()
  @Get(':id/specs')
  @ApiOperation({ summary: '상품 스펙 조회' })
  getProductSpecs(@Param('id') id: number) {
    return this.specService.getProductSpecs(id);
  }

  // ─── SPEC-02: 상품 스펙 설정 (Admin) ───
  @Put(':id/specs')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 스펙 값 설정 (Admin)' })
  setProductSpecs(@Param('id') id: number, @Body() dto: SetProductSpecsDto) {
    return this.specService.setProductSpecs(id, dto);
  }
}
