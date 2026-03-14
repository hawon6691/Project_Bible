import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Query,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { ReviewService } from './review.service';
import { Public } from '../common/decorators/public.decorator';
import { PaginationRequestDto } from '../common/dto/pagination.dto';
import { CreateReviewDto } from './dto/create-review.dto';
import { UpdateReviewDto } from './dto/update-review.dto';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { UserRole } from '../common/decorators/roles.decorator';

@ApiTags('Reviews')
@Controller()
export class ReviewController {
  constructor(private readonly reviewService: ReviewService) {}

  // REV-04: 상품 리뷰 목록
  @Public()
  @Get('products/:productId/reviews')
  @ApiOperation({ summary: '상품 리뷰 목록 조회' })
  findByProduct(
    @Param('productId') productId: number,
    @Query() query: PaginationRequestDto,
  ) {
    return this.reviewService.findByProduct(productId, query);
  }

  // REV-01: 리뷰 작성
  @Post('products/:productId/reviews')
  @ApiBearerAuth()
  @ApiOperation({ summary: '리뷰 작성' })
  create(
    @CurrentUser() user: JwtPayload,
    @Param('productId') productId: number,
    @Body() dto: CreateReviewDto,
  ) {
    return this.reviewService.create(user.sub, productId, dto);
  }

  // REV-02: 리뷰 수정
  @Patch('reviews/:id')
  @ApiBearerAuth()
  @ApiOperation({ summary: '리뷰 수정' })
  update(
    @CurrentUser() user: JwtPayload,
    @Param('id') id: number,
    @Body() dto: UpdateReviewDto,
  ) {
    return this.reviewService.update(user.sub, id, dto);
  }

  // REV-03: 리뷰 삭제 (작성자/관리자)
  @Delete('reviews/:id')
  @ApiBearerAuth()
  @ApiOperation({ summary: '리뷰 삭제' })
  remove(
    @CurrentUser() user: JwtPayload,
    @Param('id') id: number,
  ) {
    return this.reviewService.remove(user.sub, user.role as UserRole, id);
  }
}
