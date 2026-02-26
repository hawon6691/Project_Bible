import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { PaginationRequestDto } from '../common/dto/pagination.dto';
import { CreateFaqDto } from './dto/create-faq.dto';
import { CreateNoticeDto } from './dto/create-notice.dto';
import { FaqQueryDto } from './dto/faq-query.dto';
import { UpdateFaqDto } from './dto/update-faq.dto';
import { UpdateNoticeDto } from './dto/update-notice.dto';
import { FaqService } from './faq.service';

@ApiTags('FAQ')
@Controller()
export class FaqController {
  constructor(private readonly faqService: FaqService) {}

  // FAQ-01/02: FAQ 목록/검색
  @Get('faq')
  @ApiOperation({ summary: 'FAQ 목록 및 검색 조회' })
  findFaqs(@Query() query: FaqQueryDto) {
    return this.faqService.findFaqs(query);
  }

  // FAQ-03: FAQ 등록 (Admin)
  @ApiBearerAuth()
  @Post('admin/faq')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: 'FAQ 등록 (Admin)' })
  createFaq(@Body() dto: CreateFaqDto) {
    return this.faqService.createFaq(dto);
  }

  // FAQ-04: FAQ 수정 (Admin)
  @ApiBearerAuth()
  @Patch('admin/faq/:id')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: 'FAQ 수정 (Admin)' })
  updateFaq(@Param('id', ParseIntPipe) id: number, @Body() dto: UpdateFaqDto) {
    return this.faqService.updateFaq(id, dto);
  }

  // FAQ-04: FAQ 삭제 (Admin)
  @ApiBearerAuth()
  @Delete('admin/faq/:id')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: 'FAQ 삭제 (Admin)' })
  removeFaq(@Param('id', ParseIntPipe) id: number) {
    return this.faqService.removeFaq(id);
  }

  // FAQ-05: 공지사항 목록
  @Get('notices')
  @ApiOperation({ summary: '공지사항 목록 조회' })
  findNotices(@Query() query: PaginationRequestDto) {
    return this.faqService.findNotices(query);
  }

  // FAQ-06: 공지사항 등록 (Admin)
  @ApiBearerAuth()
  @Post('admin/notices')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '공지사항 등록 (Admin)' })
  createNotice(@Body() dto: CreateNoticeDto) {
    return this.faqService.createNotice(dto);
  }

  // FAQ-06: 공지사항 수정 (Admin)
  @ApiBearerAuth()
  @Patch('admin/notices/:id')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '공지사항 수정 (Admin)' })
  updateNotice(@Param('id', ParseIntPipe) id: number, @Body() dto: UpdateNoticeDto) {
    return this.faqService.updateNotice(id, dto);
  }

  // FAQ-06: 공지사항 삭제 (Admin)
  @ApiBearerAuth()
  @Delete('admin/notices/:id')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '공지사항 삭제 (Admin)' })
  removeNotice(@Param('id', ParseIntPipe) id: number) {
    return this.faqService.removeNotice(id);
  }
}
