import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  Query,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { AddPcBuildPartDto } from './dto/add-pc-build-part.dto';
import { CreateCompatibilityRuleDto } from './dto/create-compatibility-rule.dto';
import { CreatePcBuildDto } from './dto/create-pc-build.dto';
import { PcBuildListQueryDto } from './dto/pc-build-list-query.dto';
import { UpdateCompatibilityRuleDto } from './dto/update-compatibility-rule.dto';
import { UpdatePcBuildDto } from './dto/update-pc-build.dto';
import { PcBuilderService } from './pc-builder.service';

@ApiTags('PcBuilder')
@Controller()
export class PcBuilderController {
  constructor(private readonly pcBuilderService: PcBuilderService) {}

  @ApiBearerAuth()
  @Get('pc-builds')
  @ApiOperation({ summary: '내 견적 목록 조회' })
  getMyBuilds(@CurrentUser() user: JwtPayload, @Query() query: PcBuildListQueryDto) {
    return this.pcBuilderService.getMyBuilds(user.sub, query);
  }

  @ApiBearerAuth()
  @Post('pc-builds')
  @ApiOperation({ summary: '견적 생성' })
  createBuild(@CurrentUser() user: JwtPayload, @Body() dto: CreatePcBuildDto) {
    return this.pcBuilderService.createBuild(user.sub, dto);
  }

  @Public()
  @Get('pc-builds/:id')
  @ApiOperation({ summary: '견적 상세 조회' })
  getBuildDetail(@Param('id', ParseIntPipe) id: number) {
    return this.pcBuilderService.getBuildDetail(id);
  }

  @ApiBearerAuth()
  @Patch('pc-builds/:id')
  @ApiOperation({ summary: '견적 수정' })
  updateBuild(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: UpdatePcBuildDto,
  ) {
    return this.pcBuilderService.updateBuild(user.sub, id, dto);
  }

  @ApiBearerAuth()
  @Delete('pc-builds/:id')
  @ApiOperation({ summary: '견적 삭제' })
  removeBuild(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.pcBuilderService.removeBuild(user.sub, id);
  }

  @ApiBearerAuth()
  @Post('pc-builds/:id/parts')
  @ApiOperation({ summary: '견적에 부품 추가' })
  addPart(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: AddPcBuildPartDto,
  ) {
    return this.pcBuilderService.addPart(user.sub, id, dto);
  }

  @ApiBearerAuth()
  @Delete('pc-builds/:id/parts/:partId')
  @ApiOperation({ summary: '견적에서 부품 제거' })
  removePart(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Param('partId', ParseIntPipe) partId: number,
  ) {
    return this.pcBuilderService.removePart(user.sub, id, partId);
  }

  @Public()
  @Get('pc-builds/:id/compatibility')
  @ApiOperation({ summary: '호환성 체크' })
  getCompatibility(@Param('id', ParseIntPipe) id: number) {
    return this.pcBuilderService.getCompatibility(id);
  }

  @ApiBearerAuth()
  @Get('pc-builds/:id/share')
  @ApiOperation({ summary: '공유 링크 생성' })
  createShareLink(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.pcBuilderService.createShareLink(user.sub, id);
  }

  @Public()
  @Get('pc-builds/shared/:shareCode')
  @ApiOperation({ summary: '공유 견적 조회' })
  getSharedBuild(@Param('shareCode') shareCode: string) {
    return this.pcBuilderService.getSharedBuild(shareCode);
  }

  @Public()
  @Get('pc-builds/popular')
  @ApiOperation({ summary: '인기 견적 목록' })
  getPopularBuilds(@Query() query: PcBuildListQueryDto) {
    return this.pcBuilderService.getPopularBuilds(query);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Get('admin/compatibility-rules')
  @ApiOperation({ summary: '호환성 규칙 목록 (Admin)' })
  getCompatibilityRules() {
    return this.pcBuilderService.getCompatibilityRules();
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post('admin/compatibility-rules')
  @ApiOperation({ summary: '호환성 규칙 생성 (Admin)' })
  createCompatibilityRule(@Body() dto: CreateCompatibilityRuleDto) {
    return this.pcBuilderService.createCompatibilityRule(dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Patch('admin/compatibility-rules/:id')
  @ApiOperation({ summary: '호환성 규칙 수정 (Admin)' })
  updateCompatibilityRule(@Param('id', ParseIntPipe) id: number, @Body() dto: UpdateCompatibilityRuleDto) {
    return this.pcBuilderService.updateCompatibilityRule(id, dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Delete('admin/compatibility-rules/:id')
  @ApiOperation({ summary: '호환성 규칙 삭제 (Admin)' })
  removeCompatibilityRule(@Param('id', ParseIntPipe) id: number) {
    return this.pcBuilderService.removeCompatibilityRule(id);
  }
}
