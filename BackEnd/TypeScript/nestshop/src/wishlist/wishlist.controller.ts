import {
  Controller,
  Delete,
  Get,
  Param,
  Post,
  Query,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { WishlistService } from './wishlist.service';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { PaginationRequestDto } from '../common/dto/pagination.dto';

@ApiTags('Wishlist')
@Controller('wishlist')
@ApiBearerAuth()
export class WishlistController {
  constructor(private readonly wishlistService: WishlistService) {}

  @Get()
  @ApiOperation({ summary: '내 위시리스트 조회' })
  findMyWishlist(@CurrentUser() user: JwtPayload, @Query() query: PaginationRequestDto) {
    return this.wishlistService.findMyWishlist(user.sub, query);
  }

  @Post(':productId')
  @ApiOperation({ summary: '위시리스트 토글' })
  toggle(@CurrentUser() user: JwtPayload, @Param('productId') productId: number) {
    return this.wishlistService.toggle(user.sub, productId);
  }

  @Delete(':productId')
  @ApiOperation({ summary: '위시리스트 삭제' })
  remove(@CurrentUser() user: JwtPayload, @Param('productId') productId: number) {
    return this.wishlistService.remove(user.sub, productId);
  }
}
