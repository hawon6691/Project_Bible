import { Body, Controller, Get, Param, ParseIntPipe, Patch, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { ApproveMappingDto } from './dto/approve-mapping.dto';
import { MappingPendingQueryDto } from './dto/mapping-pending-query.dto';
import { RejectMappingDto } from './dto/reject-mapping.dto';
import { MatchingService } from './matching.service';

@ApiTags('Matching')
@ApiBearerAuth()
@Roles(UserRole.ADMIN)
@Controller('matching')
export class MatchingController {
  constructor(private readonly matchingService: MatchingService) {}

  @Get('pending')
  @ApiOperation({ summary: '매핑 대기 목록 조회 (Admin)' })
  getPendingList(@Query() query: MappingPendingQueryDto) {
    return this.matchingService.getPendingList(query);
  }

  @Patch(':id/approve')
  @ApiOperation({ summary: '매핑 승인 (Admin)' })
  approveMapping(
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: ApproveMappingDto,
    @CurrentUser() user: JwtPayload,
  ) {
    return this.matchingService.approveMapping(id, dto, user.sub);
  }

  @Patch(':id/reject')
  @ApiOperation({ summary: '매핑 거절 (Admin)' })
  rejectMapping(
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: RejectMappingDto,
    @CurrentUser() user: JwtPayload,
  ) {
    return this.matchingService.rejectMapping(id, dto, user.sub);
  }

  @Post('auto-match')
  @ApiOperation({ summary: '자동 매핑 실행 (Admin)' })
  autoMatch(@CurrentUser() user: JwtPayload) {
    return this.matchingService.autoMatch(user.sub);
  }

  @Get('stats')
  @ApiOperation({ summary: '매핑 통계 조회 (Admin)' })
  getStats() {
    return this.matchingService.getStats();
  }
}
