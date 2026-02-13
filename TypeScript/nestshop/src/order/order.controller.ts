import {
  Controller, Get, Post, Patch, Body, Param, Query, HttpCode, HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { OrderService } from './order.service';
import { CreateOrderDto } from './dto/create-order.dto';
import { CreatePaymentDto, RefundPaymentDto } from './dto/payment.dto';
import { OrderStatus } from './entities/order.entity';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { PaginationRequestDto } from '../common/dto/pagination.dto';

@ApiTags('Orders')
@Controller()
@ApiBearerAuth()
export class OrderController {
  constructor(private readonly orderService: OrderService) {}

  // ORD-01: 주문 생성
  @Post('orders')
  @ApiOperation({ summary: '주문 생성' })
  create(@CurrentUser() user: JwtPayload, @Body() dto: CreateOrderDto) {
    return this.orderService.create(user.sub, dto);
  }

  // ORD-02: 내 주문 목록
  @Get('orders')
  @ApiOperation({ summary: '내 주문 목록' })
  findMyOrders(@CurrentUser() user: JwtPayload, @Query() query: PaginationRequestDto) {
    return this.orderService.findMyOrders(user.sub, query);
  }

  // ORD-03: 주문 상세 조회
  @Get('orders/:id')
  @ApiOperation({ summary: '주문 상세 조회' })
  findOne(@CurrentUser() user: JwtPayload, @Param('id') id: number) {
    return this.orderService.findOne(user.sub, id);
  }

  // ORD-04: 주문 취소
  @Post('orders/:id/cancel')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '주문 취소' })
  cancel(@CurrentUser() user: JwtPayload, @Param('id') id: number) {
    return this.orderService.cancel(user.sub, id);
  }

  // PAY-01: 결제 요청 (모의 결제)
  @Post('payments')
  @ApiOperation({ summary: '결제 요청 (모의 결제)' })
  requestPayment(@CurrentUser() user: JwtPayload, @Body() dto: CreatePaymentDto) {
    return this.orderService.requestPayment(user.sub, dto);
  }

  // PAY-02: 결제 상태 조회
  @Get('payments/:id')
  @ApiOperation({ summary: '결제 상태 조회' })
  getPayment(@CurrentUser() user: JwtPayload, @Param('id') id: number) {
    return this.orderService.getPayment(user.sub, id);
  }

  // PAY-03: 환불 처리
  @Post('payments/:id/refund')
  @ApiOperation({ summary: '환불 처리' })
  refundPayment(
    @CurrentUser() user: JwtPayload,
    @Param('id') id: number,
    @Body() dto: RefundPaymentDto,
  ) {
    return this.orderService.refundPayment(user.sub, id, dto, false);
  }

  // PAY-03: 환불 처리 (관리자)
  @Post('admin/payments/:id/refund')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '환불 처리 (Admin)' })
  adminRefundPayment(
    @CurrentUser() admin: JwtPayload,
    @Param('id') id: number,
    @Body() dto: RefundPaymentDto,
  ) {
    return this.orderService.refundPayment(admin.sub, id, dto, true);
  }

  // ORD-05: 전체 주문 관리 (Admin)
  @Get('admin/orders')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '전체 주문 관리 (Admin)' })
  findAllOrders(@Query() query: PaginationRequestDto) {
    return this.orderService.findAllOrders(query);
  }

  // ORD-06: 주문 상태 변경 (Admin)
  @Patch('admin/orders/:id/status')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '주문 상태 변경 (Admin)' })
  updateStatus(@Param('id') id: number, @Body('status') status: OrderStatus) {
    return this.orderService.updateStatus(id, status);
  }
}
