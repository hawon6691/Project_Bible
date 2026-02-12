import {
  Controller,
  Get,
  Put,
  Patch,
  Delete,
  Body,
  Param,
  Query,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { UserService } from './user.service';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { Public } from '../common/decorators/public.decorator';
import { UpdateUserDto } from './dto/update-user.dto';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { PaginationRequestDto } from '../common/dto/pagination.dto';
import { UserStatus } from './entities/user.entity';

@ApiTags('Users')
@Controller('users')
export class UserController {
  constructor(private readonly userService: UserService) {}

  // ─── USER-01: 내 정보 조회 ───
  @Get('me')
  @ApiBearerAuth()
  @ApiOperation({ summary: '내 정보 조회' })
  getMe(@CurrentUser() user: JwtPayload) {
    return this.userService.getMe(user.sub);
  }

  // ─── USER-02: 내 정보 수정 ───
  @Put('me')
  @ApiBearerAuth()
  @ApiOperation({ summary: '내 정보 수정 (이름, 전화번호, 비밀번호)' })
  updateMe(@CurrentUser() user: JwtPayload, @Body() dto: UpdateUserDto) {
    return this.userService.updateMe(user.sub, dto);
  }

  // ─── USER-03: 회원 탈퇴 ───
  @Delete('me')
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '회원 탈퇴 (소프트 삭제)' })
  deleteMe(@CurrentUser() user: JwtPayload) {
    return this.userService.deleteMe(user.sub);
  }

  // ─── USER-06: 프로필 조회 ───
  @Public()
  @Get('profile/:id')
  @ApiOperation({ summary: '프로필 조회 (본인 또는 타인)' })
  getProfile(@Param('id') id: number) {
    return this.userService.getProfile(id);
  }

  // ─── USER-07, 08: 닉네임/소개글 수정 ───
  @Patch('me/profile')
  @ApiBearerAuth()
  @ApiOperation({ summary: '닉네임/소개글 수정' })
  updateProfile(@CurrentUser() user: JwtPayload, @Body() dto: UpdateProfileDto) {
    return this.userService.updateProfile(user.sub, dto);
  }

  // ─── USER-09: 프로필 이미지 삭제 ───
  @Delete('me/profile-image')
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: '프로필 이미지 삭제 (기본 이미지로 변경)' })
  deleteProfileImage(@CurrentUser() user: JwtPayload) {
    return this.userService.updateProfileImage(user.sub, null);
  }

  // ─── USER-04: 회원 목록 조회 (Admin) ───
  @Get()
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '회원 목록 조회 (Admin)' })
  findAll(@Query() query: PaginationRequestDto) {
    return this.userService.findAll(query);
  }

  // ─── USER-05: 회원 상태 변경 (Admin) ───
  @Patch(':id/status')
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: '회원 상태 변경 (Admin)' })
  updateStatus(
    @Param('id') id: number,
    @Body('status') status: UserStatus,
  ) {
    return this.userService.updateStatus(id, status);
  }
}
