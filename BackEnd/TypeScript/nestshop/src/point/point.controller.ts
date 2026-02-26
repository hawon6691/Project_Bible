import { Body, Controller, Get, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { PointService } from './point.service';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { PaginationRequestDto } from '../common/dto/pagination.dto';
import { AdminGrantPointDto } from './dto/admin-grant-point.dto';
import { Roles, UserRole } from '../common/decorators/roles.decorator';

@ApiTags('Points')
@Controller()
@ApiBearerAuth()
export class PointController {
  constructor(private readonly pointService: PointService) {}

  // PNT-01: 포인트 잔액 조회
  @Get('points/balance')
  @ApiOperation({ summary: '내 포인트 잔액 조회' })
  getBalance(@CurrentUser() user: JwtPayload) {
    return this.pointService.getBalance(user.sub);
  }

  // PNT-02: 포인트 내역 조회
  @Get('points/transactions')
  @ApiOperation({ summary: '내 포인트 내역 조회' })
  getTransactions(@CurrentUser() user: JwtPayload, @Query() query: PaginationRequestDto) {
    return this.pointService.getTransactions(user.sub, query);
  }

  // PNT-06: 관리자 포인트 지급
  @Post('admin/points/grant')
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: '관리자 포인트 수동 지급' })
  adminGrant(@CurrentUser() admin: JwtPayload, @Body() dto: AdminGrantPointDto) {
    return this.pointService.adminGrant(admin.sub, dto);
  }
}
