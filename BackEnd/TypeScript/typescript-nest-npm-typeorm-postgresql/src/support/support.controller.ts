import {
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Post,
  Query,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { AnswerSupportTicketDto } from './dto/answer-support-ticket.dto';
import { CreateSupportTicketDto } from './dto/create-support-ticket.dto';
import { SupportTicketQueryDto } from './dto/support-ticket-query.dto';
import { SupportService } from './support.service';

@ApiTags('Support')
@Controller()
@ApiBearerAuth()
export class SupportController {
  constructor(private readonly supportService: SupportService) {}

  // SUP-01: 1:1 문의 작성
  @Post('support/tickets')
  @ApiOperation({ summary: '1:1 문의 작성' })
  create(@CurrentUser() user: JwtPayload, @Body() dto: CreateSupportTicketDto) {
    return this.supportService.create(user.sub, dto);
  }

  // SUP-02: 내 문의 목록
  @Get('support/tickets/me')
  @ApiOperation({ summary: '내 문의 목록 조회' })
  findMine(@CurrentUser() user: JwtPayload, @Query() query: SupportTicketQueryDto) {
    return this.supportService.findMine(user.sub, query);
  }

  // SUP-03: 내 문의 상세
  @Get('support/tickets/me/:id')
  @ApiOperation({ summary: '내 문의 상세 조회' })
  findMyOne(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.supportService.findMyOne(user.sub, id);
  }

  // SUP-04: 문의 답변 (Admin)
  @Post('admin/support/tickets/:id/answer')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '문의 답변 작성 (Admin)' })
  answer(
    @CurrentUser() admin: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: AnswerSupportTicketDto,
  ) {
    return this.supportService.answer(admin.sub, id, dto);
  }

  // SUP-05: 전체 문의 관리 (Admin)
  @Get('admin/support/tickets')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '전체 문의 목록 조회 (Admin)' })
  findAll(@Query() query: SupportTicketQueryDto) {
    return this.supportService.findAll(query);
  }
}
