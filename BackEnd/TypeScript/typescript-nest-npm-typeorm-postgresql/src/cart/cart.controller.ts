import {
  Body,
  Controller,
  Delete,
  Get,
  Headers,
  HttpCode,
  HttpStatus,
  Param,
  ParseIntPipe,
  Patch,
  Post,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { AddCartItemDto, MergeGuestCartDto, UpdateCartQuantityDto } from './dto/cart.dto';
import { CartService } from './cart.service';

@ApiTags('Cart')
@Controller('cart')
export class CartController {
  constructor(private readonly cartService: CartService) {}

  @Get()
  @ApiBearerAuth()
  @ApiOperation({ summary: '회원 장바구니 조회' })
  getCart(@CurrentUser() user: JwtPayload) {
    return this.cartService.getCart(user.sub);
  }

  @Post()
  @ApiBearerAuth()
  @ApiOperation({ summary: '회원 장바구니 추가' })
  addItem(@CurrentUser() user: JwtPayload, @Body() dto: AddCartItemDto) {
    return this.cartService.addItem(user.sub, dto);
  }

  @Patch(':itemId')
  @ApiBearerAuth()
  @ApiOperation({ summary: '회원 장바구니 수량 변경' })
  updateQuantity(
    @CurrentUser() user: JwtPayload,
    @Param('itemId', ParseIntPipe) itemId: number,
    @Body() dto: UpdateCartQuantityDto,
  ) {
    return this.cartService.updateQuantity(user.sub, itemId, dto);
  }

  @Delete(':itemId')
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '회원 장바구니 항목 삭제' })
  removeItem(@CurrentUser() user: JwtPayload, @Param('itemId', ParseIntPipe) itemId: number) {
    return this.cartService.removeItem(user.sub, itemId);
  }

  @Delete()
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '회원 장바구니 전체 비우기' })
  clearCart(@CurrentUser() user: JwtPayload) {
    return this.cartService.clearCart(user.sub);
  }

  // ACT-06: 비회원 장바구니 조회/추가/변경/삭제/비우기
  @Public()
  @Get('guest')
  @ApiOperation({ summary: '비회원 장바구니 조회 (x-cart-key)' })
  getGuestCart(@Headers('x-cart-key') guestCartKey: string | undefined) {
    return this.cartService.getGuestCart(guestCartKey ?? '');
  }

  @Public()
  @Post('guest')
  @ApiOperation({ summary: '비회원 장바구니 추가 (x-cart-key)' })
  addGuestItem(
    @Headers('x-cart-key') guestCartKey: string | undefined,
    @Body() dto: AddCartItemDto,
  ) {
    return this.cartService.addGuestItem(guestCartKey ?? '', dto);
  }

  @Public()
  @Patch('guest/:itemId')
  @ApiOperation({ summary: '비회원 장바구니 수량 변경 (x-cart-key)' })
  updateGuestQuantity(
    @Headers('x-cart-key') guestCartKey: string | undefined,
    @Param('itemId') itemId: string,
    @Body() dto: UpdateCartQuantityDto,
  ) {
    return this.cartService.updateGuestQuantity(guestCartKey ?? '', itemId, dto);
  }

  @Public()
  @Delete('guest/:itemId')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '비회원 장바구니 항목 삭제 (x-cart-key)' })
  removeGuestItem(
    @Headers('x-cart-key') guestCartKey: string | undefined,
    @Param('itemId') itemId: string,
  ) {
    return this.cartService.removeGuestItem(guestCartKey ?? '', itemId);
  }

  @Public()
  @Delete('guest')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '비회원 장바구니 전체 비우기 (x-cart-key)' })
  clearGuestCart(@Headers('x-cart-key') guestCartKey: string | undefined) {
    return this.cartService.clearGuestCart(guestCartKey ?? '');
  }

  @Post('guest/merge')
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '로그인 사용자 장바구니와 비회원 장바구니 병합' })
  mergeGuestCart(@CurrentUser() user: JwtPayload, @Body() dto: MergeGuestCartDto) {
    return this.cartService.mergeGuestCartToUser(user.sub, dto.guestCartKey);
  }
}
