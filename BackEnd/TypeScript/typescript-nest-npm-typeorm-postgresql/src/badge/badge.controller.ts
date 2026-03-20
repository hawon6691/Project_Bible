import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { BadgeService } from './badge.service';
import { CreateBadgeDto } from './dto/create-badge.dto';
import { GrantBadgeDto } from './dto/grant-badge.dto';
import { UpdateBadgeDto } from './dto/update-badge.dto';

@ApiTags('Badge')
@Controller()
export class BadgeController {
  constructor(private readonly badgeService: BadgeService) {}

  @Public()
  @Get('badges')
  @ApiOperation({ summary: '전체 배지 목록 조회' })
  getAllBadges() {
    return this.badgeService.getAllBadges();
  }

  @ApiBearerAuth()
  @Get('badges/me')
  @ApiOperation({ summary: '내 배지 목록 조회' })
  getMyBadges(@CurrentUser() user: JwtPayload) {
    return this.badgeService.getMyBadges(user.sub);
  }

  @Public()
  @Get('users/:id/badges')
  @ApiOperation({ summary: '특정 유저 배지 목록 조회' })
  getUserBadges(@Param('id', ParseIntPipe) userId: number) {
    return this.badgeService.getUserBadges(userId);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post('admin/badges')
  @ApiOperation({ summary: '배지 생성 (Admin)' })
  create(@Body() dto: CreateBadgeDto) {
    return this.badgeService.create(dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Patch('admin/badges/:id')
  @ApiOperation({ summary: '배지 수정 (Admin)' })
  update(@Param('id', ParseIntPipe) id: number, @Body() dto: UpdateBadgeDto) {
    return this.badgeService.update(id, dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Delete('admin/badges/:id')
  @ApiOperation({ summary: '배지 삭제 (Admin)' })
  remove(@Param('id', ParseIntPipe) id: number) {
    return this.badgeService.remove(id);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post('admin/badges/:id/grant')
  @ApiOperation({ summary: '배지 수동 부여 (Admin)' })
  grant(
    @Param('id', ParseIntPipe) badgeId: number,
    @Body() dto: GrantBadgeDto,
    @CurrentUser() user: JwtPayload,
  ) {
    return this.badgeService.grant(badgeId, dto, user.sub);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Delete('admin/badges/:id/revoke/:userId')
  @ApiOperation({ summary: '배지 회수 (Admin)' })
  revoke(@Param('id', ParseIntPipe) badgeId: number, @Param('userId', ParseIntPipe) userId: number) {
    return this.badgeService.revoke(badgeId, userId);
  }
}
