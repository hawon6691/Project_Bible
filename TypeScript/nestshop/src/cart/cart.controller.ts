import {
  Controller, Get, Post, Patch, Delete,
  Body, Param, HttpCode, HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { CartService } from './cart.service';
import { AddCartItemDto, UpdateCartQuantityDto } from './dto/cart.dto';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';

@ApiTags('Cart')
@Controller('cart')
@ApiBearerAuth()
export class CartController {
  constructor(private readonly cartService: CartService) {}

  @Get()
  @ApiOperation({ summary: '장바구니 조회' })
  getCart(@CurrentUser() user: JwtPayload) {
    return this.cartService.getCart(user.sub);
  }

  @Post()
  @ApiOperation({ summary: '장바구니 추가' })
  addItem(@CurrentUser() user: JwtPayload, @Body() dto: AddCartItemDto) {
    return this.cartService.addItem(user.sub, dto);
  }

  @Patch(':itemId')
  @ApiOperation({ summary: '장바구니 수량 변경' })
  updateQuantity(
    @CurrentUser() user: JwtPayload,
    @Param('itemId') itemId: number,
    @Body() dto: UpdateCartQuantityDto,
  ) {
    return this.cartService.updateQuantity(user.sub, itemId, dto);
  }

  @Delete(':itemId')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '장바구니 항목 삭제' })
  removeItem(@CurrentUser() user: JwtPayload, @Param('itemId') itemId: number) {
    return this.cartService.removeItem(user.sub, itemId);
  }

  @Delete()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '장바구니 전체 비우기' })
  clearCart(@CurrentUser() user: JwtPayload) {
    return this.cartService.clearCart(user.sub);
  }
}
