import {
  Controller, Get, Post, Patch, Delete, Body, Param, Query,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { ProductService } from './product.service';
import { CreateProductDto, CreateProductOptionDto, CreateProductImageDto } from './dto/create-product.dto';
import { UpdateProductDto } from './dto/update-product.dto';
import { ProductQueryDto } from './dto/product-query.dto';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';

@ApiTags('Products')
@Controller()
export class ProductController {
  constructor(private readonly productService: ProductService) {}

  // ─── PROD-01: 상품 목록 조회 ───
  @Get('products')
  @Public()
  @ApiOperation({ summary: '상품 목록 조회' })
  findAll(@Query() query: ProductQueryDto) {
    return this.productService.findAll(query);
  }

  // ─── PROD-02: 상품 상세 조회 ───
  @Get('products/:id')
  @Public()
  @ApiOperation({ summary: '상품 상세 조회' })
  findOne(@Param('id') id: number) {
    return this.productService.findOne(id);
  }

  // ─── PROD-03: 상품 등록 ───
  @Post('products')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 등록 (Admin)' })
  create(@Body() dto: CreateProductDto) {
    return this.productService.create(dto);
  }

  // ─── PROD-04: 상품 수정 ───
  @Patch('products/:id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 수정 (Admin)' })
  update(@Param('id') id: number, @Body() dto: UpdateProductDto) {
    return this.productService.update(id, dto);
  }

  // ─── PROD-05: 상품 삭제 ───
  @Delete('products/:id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 삭제 (Admin)' })
  remove(@Param('id') id: number) {
    return this.productService.remove(id);
  }

  // ─── PROD-06: 옵션 추가 ───
  @Post('products/:id/options')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 옵션 추가 (Admin)' })
  addOption(@Param('id') id: number, @Body() dto: CreateProductOptionDto) {
    return this.productService.addOption(id, dto);
  }

  // ─── PROD-06: 옵션 수정 ───
  @Patch('products/:id/options/:optionId')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 옵션 수정 (Admin)' })
  updateOption(
    @Param('id') id: number,
    @Param('optionId') optionId: number,
    @Body() dto: CreateProductOptionDto,
  ) {
    return this.productService.updateOption(id, optionId, dto);
  }

  // ─── PROD-06: 옵션 삭제 ───
  @Delete('products/:id/options/:optionId')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 옵션 삭제 (Admin)' })
  removeOption(@Param('id') id: number, @Param('optionId') optionId: number) {
    return this.productService.removeOption(id, optionId);
  }

  // ─── PROD-07: 이미지 추가 ───
  @Post('products/:id/images')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 이미지 추가 (Admin)' })
  addImage(@Param('id') id: number, @Body() dto: CreateProductImageDto) {
    return this.productService.addImage(id, dto.url, dto.isMain, dto.sortOrder);
  }

  // ─── PROD-07: 이미지 삭제 ───
  @Delete('products/:id/images/:imageId')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '상품 이미지 삭제 (Admin)' })
  removeImage(@Param('id') id: number, @Param('imageId') imageId: number) {
    return this.productService.removeImage(id, imageId);
  }
}
