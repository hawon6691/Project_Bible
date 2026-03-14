import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Body,
  Param,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { CategoryService } from './category.service';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { Public } from '../common/decorators/public.decorator';

@ApiTags('Categories')
@Controller('categories')
export class CategoryController {
  constructor(private readonly categoryService: CategoryService) {}

  // ─── CAT-01: 카테고리 목록 조회 (트리) ───
  @Public()
  @Get()
  @ApiOperation({ summary: '카테고리 전체 목록 (트리 구조)' })
  findAll() {
    return this.categoryService.findAllTree();
  }

  // ─── CAT-01: 단일 카테고리 조회 ───
  @Public()
  @Get(':id')
  @ApiOperation({ summary: '카테고리 단일 조회' })
  findOne(@Param('id') id: number) {
    return this.categoryService.findOne(id);
  }

  // ─── CAT-02: 카테고리 생성 (Admin) ───
  @Post()
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '카테고리 생성 (Admin)' })
  create(@Body() dto: CreateCategoryDto) {
    return this.categoryService.create(dto);
  }

  // ─── CAT-03: 카테고리 수정 (Admin) ───
  @Patch(':id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '카테고리 수정 (Admin)' })
  update(@Param('id') id: number, @Body() dto: UpdateCategoryDto) {
    return this.categoryService.update(id, dto);
  }

  // ─── CAT-04: 카테고리 삭제 (Admin) ───
  @Delete(':id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '카테고리 삭제 (Admin, 하위 없을 때만)' })
  remove(@Param('id') id: number) {
    return this.categoryService.remove(id);
  }
}
