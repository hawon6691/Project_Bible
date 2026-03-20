import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Post,
  Query,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { AnswerInquiryDto } from './dto/answer-inquiry.dto';
import { CreateInquiryDto } from './dto/create-inquiry.dto';
import { InquiryQueryDto } from './dto/inquiry-query.dto';
import { InquiryService } from './inquiry.service';

@ApiTags('Inquiries')
@Controller()
export class InquiryController {
  constructor(private readonly inquiryService: InquiryService) {}

  // INQ-01: 상품 문의 작성
  @ApiBearerAuth()
  @Post('products/:productId/inquiries')
  @ApiOperation({ summary: '상품 문의 작성' })
  create(
    @CurrentUser() user: JwtPayload,
    @Param('productId', ParseIntPipe) productId: number,
    @Body() dto: CreateInquiryDto,
  ) {
    return this.inquiryService.create(user.sub, productId, dto);
  }

  // INQ-02: 상품 문의 목록
  @Public()
  @Get('products/:productId/inquiries')
  @ApiOperation({ summary: '상품 문의 목록 조회' })
  findByProduct(
    @Param('productId', ParseIntPipe) productId: number,
    @Query() query: InquiryQueryDto,
  ) {
    return this.inquiryService.findByProduct(productId, query);
  }

  // INQ-03: 문의 답변
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN, UserRole.SELLER)
  @Post('inquiries/:id/answer')
  @ApiOperation({ summary: '문의 답변 작성 (관리자/판매자)' })
  answer(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: AnswerInquiryDto,
  ) {
    return this.inquiryService.answer(id, user.sub, user.role as UserRole, dto);
  }

  // INQ-04: 내 문의 목록
  @ApiBearerAuth()
  @Get('inquiries/me')
  @ApiOperation({ summary: '내 문의 목록 조회' })
  findMine(
    @CurrentUser() user: JwtPayload,
    @Query() query: InquiryQueryDto,
  ) {
    return this.inquiryService.findMine(user.sub, query);
  }

  // INQ-05: 문의 삭제
  @ApiBearerAuth()
  @Delete('inquiries/:id')
  @ApiOperation({ summary: '문의 삭제 (답변 전 작성자만)' })
  remove(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.inquiryService.remove(user.sub, id);
  }
}
