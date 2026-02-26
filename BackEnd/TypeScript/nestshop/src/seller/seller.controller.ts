import {
  Controller, Get, Post, Patch, Delete,
  Body, Param, Query, HttpCode, HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { SellerService } from './seller.service';
import { CreateSellerDto } from './dto/create-seller.dto';
import { UpdateSellerDto } from './dto/update-seller.dto';
import { PaginationRequestDto } from '../common/dto/pagination.dto';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { Public } from '../common/decorators/public.decorator';

@ApiTags('Sellers')
@Controller('sellers')
export class SellerController {
  constructor(private readonly sellerService: SellerService) {}

  @Public()
  @Get()
  @ApiOperation({ summary: '판매처 목록 조회' })
  findAll(@Query() query: PaginationRequestDto) {
    return this.sellerService.findAll(query);
  }

  @Public()
  @Get(':id')
  @ApiOperation({ summary: '판매처 상세 조회' })
  findOne(@Param('id') id: number) {
    return this.sellerService.findOne(id);
  }

  @Post()
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '판매처 등록 (Admin)' })
  create(@Body() dto: CreateSellerDto) {
    return this.sellerService.create(dto);
  }

  @Patch(':id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '판매처 수정 (Admin)' })
  update(@Param('id') id: number, @Body() dto: UpdateSellerDto) {
    return this.sellerService.update(id, dto);
  }

  @Delete(':id')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '판매처 삭제 (Admin)' })
  remove(@Param('id') id: number) {
    return this.sellerService.remove(id);
  }
}
