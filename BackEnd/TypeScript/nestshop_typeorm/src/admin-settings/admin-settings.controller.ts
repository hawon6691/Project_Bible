import { Body, Controller, Get, Patch, Post } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { AdminSettingsService } from './admin-settings.service';
import { SetAllowedExtensionsDto } from './dto/set-allowed-extensions.dto';
import { UpdateReviewPolicyDto } from './dto/update-review-policy.dto';
import { UpdateUploadLimitsDto } from './dto/update-upload-limits.dto';

@ApiTags('AdminSettings')
@ApiBearerAuth()
@Roles(UserRole.ADMIN)
@Controller('admin/settings')
export class AdminSettingsController {
  constructor(private readonly adminSettingsService: AdminSettingsService) {}

  @Get('extensions')
  @ApiOperation({ summary: '허용 확장자 목록 조회 (Admin)' })
  getAllowedExtensions() {
    return this.adminSettingsService.getAllowedExtensions();
  }

  @Post('extensions')
  @ApiOperation({ summary: '허용 확장자 추가/변경 (Admin)' })
  setAllowedExtensions(@Body() dto: SetAllowedExtensionsDto) {
    return this.adminSettingsService.setAllowedExtensions(dto);
  }

  @Get('upload-limits')
  @ApiOperation({ summary: '업로드 용량 제한 조회 (Admin)' })
  getUploadLimits() {
    return this.adminSettingsService.getUploadLimits();
  }

  @Patch('upload-limits')
  @ApiOperation({ summary: '업로드 용량 제한 변경 (Admin)' })
  updateUploadLimits(@Body() dto: UpdateUploadLimitsDto) {
    return this.adminSettingsService.updateUploadLimits(dto);
  }

  @Get('review-policy')
  @ApiOperation({ summary: '리뷰 정책 조회 (Admin)' })
  getReviewPolicy() {
    return this.adminSettingsService.getReviewPolicy();
  }

  @Patch('review-policy')
  @ApiOperation({ summary: '리뷰 정책 변경 (Admin)' })
  updateReviewPolicy(@Body() dto: UpdateReviewPolicyDto) {
    return this.adminSettingsService.updateReviewPolicy(dto);
  }
}
