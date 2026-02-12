import {
  Controller, Get, Post, Patch, Put, Delete, Body, Param, Query,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { SpecService } from './spec.service';
import { CreateSpecDefinitionDto } from './dto/create-spec-definition.dto';
import { UpdateSpecDefinitionDto } from './dto/update-spec-definition.dto';
import { SetProductSpecsDto } from './dto/set-product-specs.dto';
import { CompareSpecsDto, ScoredCompareDto, SetSpecScoresDto } from './dto/compare-specs.dto';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';

// ─── 스펙 정의 & 비교 컨트롤러 ───
@ApiTags('Specs')
@Controller('specs')
export class SpecController {
  constructor(private readonly specService: SpecService) {}

  @Get('definitions')
  @Public()
  @ApiOperation({ summary: '카테고리별 스펙 정의 목록' })
  findDefinitions(@Query('categoryId') categoryId?: number) {
    return this.specService.findDefinitions(categoryId);
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
  updateDefinition(@Param('id') id: number, @Body() dto: UpdateSpecDefinitionDto) {
    return this.specService.updateDefinition(id, dto);
  }

  @Delete('definitions/:id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '스펙 정의 삭제 (Admin)' })
  removeDefinition(@Param('id') id: number) {
    return this.specService.removeDefinition(id);
  }

  @Post('compare')
  @Public()
  @ApiOperation({ summary: '상품 스펙 비교 (2~4개)' })
  compareSpecs(@Body() dto: CompareSpecsDto) {
    return this.specService.compareSpecs(dto);
  }

  @Post('compare/scored')
  @Public()
  @ApiOperation({ summary: '점수화 스펙 비교' })
  scoredCompare(@Body() dto: ScoredCompareDto) {
    return this.specService.scoredCompare(dto);
  }

  @Put('scores/:specDefId')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '스펙 점수 매핑 설정 (Admin)' })
  setScores(@Param('specDefId') specDefId: number, @Body() dto: SetSpecScoresDto) {
    return this.specService.setScores(specDefId, dto);
  }
}

// ─── 상품별 스펙 컨트롤러 ───
@ApiTags('Product Specs')
@Controller('products')
export class ProductSpecController {
  constructor(private readonly specService: SpecService) {}

  @Get(':id/specs')
  @Public()
  @ApiOperation({ summary: '상품 스펙 조회' })
  getProductSpecs(@Param('id') id: number) {
    return this.specService.getProductSpecs(id);
  }

  @Put(':id/specs')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 스펙 설정 (Admin)' })
  setProductSpecs(@Param('id') id: number, @Body() dto: SetProductSpecsDto) {
    return this.specService.setProductSpecs(id, dto);
  }
}
