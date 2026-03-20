import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { CreateDealDto } from './dto/create-deal.dto';
import { DealQueryDto } from './dto/deal-query.dto';
import { UpdateDealDto } from './dto/update-deal.dto';
import { DealService } from './deal.service';

@ApiTags('Deal')
@Controller('deals')
export class DealController {
  constructor(private readonly dealService: DealService) {}

  // DEAL-01: 특가 목록 조회
  @Public()
  @Get()
  @ApiOperation({ summary: '특가 목록 조회' })
  findDeals(@Query() query: DealQueryDto) {
    return this.dealService.findDeals(query);
  }

  // DEAL-02: 특가 등록
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post('admin')
  @ApiOperation({ summary: '특가 등록 (Admin)' })
  create(@Body() dto: CreateDealDto) {
    return this.dealService.create(dto);
  }

  // DEAL-03: 특가 수정
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Patch('admin/:id')
  @ApiOperation({ summary: '특가 수정 (Admin)' })
  update(@Param('id', ParseIntPipe) id: number, @Body() dto: UpdateDealDto) {
    return this.dealService.update(id, dto);
  }

  // DEAL-04: 특가 삭제
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Delete('admin/:id')
  @ApiOperation({ summary: '특가 삭제 (Admin)' })
  remove(@Param('id', ParseIntPipe) id: number) {
    return this.dealService.remove(id);
  }
}
