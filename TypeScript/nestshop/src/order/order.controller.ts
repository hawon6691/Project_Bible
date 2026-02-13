import {
  Controller, Get, Post, Patch, Body, Param, Query, HttpCode, HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { OrderService } from './order.service';
import { CreateOrderDto } from './dto/create-order.dto';
import { OrderStatus } from './entities/order.entity';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { PaginationRequestDto } from '../common/dto/pagination.dto';

@ApiTags('Orders')
@Controller()
@ApiBearerAuth()
export class OrderController {
  constructor(private readonly orderService: OrderService) {}

  // ─── ORD-01: 주문 생성 ───
  @Post('orders')
  @ApiOperation({ summary: '주문 생성' })
  create(@CurrentUser() user: JwtPayload, @Body() dto: CreateOrderDto) {
    return this.orderService.create(user.sub, dto);
  }

  // ─── ORD-02: 내 주문 목록 ───
  @Get('orders')
  @ApiOperation({ summary: '내 주문 목록' })
  findMyOrders(@CurrentUser() user: JwtPayload, @Query() query: PaginationRequestDto) {
    return this.orderService.findMyOrders(user.sub, query);
  }

  // ─── ORD-03: 주문 상세 ───
  @Get('orders/:id')
  @ApiOperation({ summary: '주문 상세 조회' })
  findOne(@CurrentUser() user: JwtPayload, @Param('id') id: number) {
    return this.orderService.findOne(user.sub, id);
  }

  // ─── ORD-04: 주문 취소 ───
  @Post('orders/:id/cancel')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '주문 취소' })
  cancel(@CurrentUser() user: JwtPayload, @Param('id') id: number) {
    return this.orderService.cancel(user.sub, id);
  }

  // ─── ORD-05: 전체 주문 관리 (Admin) ───
  @Get('admin/orders')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '전체 주문 관리 (Admin)' })
  findAllOrders(@Query() query: PaginationRequestDto) {
    return this.orderService.findAllOrders(query);
  }

  // ─── ORD-06: 주문 상태 변경 (Admin) ───
  @Patch('admin/orders/:id/status')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '주문 상태 변경 (Admin)' })
  updateStatus(@Param('id') id: number, @Body('status') status: OrderStatus) {
    return this.orderService.updateStatus(id, status);
  }
}
