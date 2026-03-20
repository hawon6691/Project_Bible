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
import { UserRole } from '../common/decorators/roles.decorator';
import { CommunityPostQueryDto } from './dto/community-post-query.dto';
import { CreateCommunityPostDto } from './dto/create-community-post.dto';
import { UpdateCommunityPostDto } from './dto/update-community-post.dto';
import { CommunityService } from './community.service';

@ApiTags('Community')
@Controller('community')
export class CommunityController {
  constructor(private readonly communityService: CommunityService) {}

  // COMM-01: 게시판 목록
  @Public()
  @Get('boards')
  @ApiOperation({ summary: '게시판 목록 조회' })
  getBoards() {
    return this.communityService.getBoards();
  }

  // COMM-02: 게시글 목록
  @Public()
  @Get('posts')
  @ApiOperation({ summary: '게시글 목록 조회' })
  findPosts(@Query() query: CommunityPostQueryDto) {
    return this.communityService.findPosts(query);
  }

  // COMM-03: 게시글 상세
  @Public()
  @Get('posts/:id')
  @ApiOperation({ summary: '게시글 상세 조회' })
  findOne(@Param('id', ParseIntPipe) id: number) {
    return this.communityService.findOne(id);
  }

  // COMM-04: 게시글 작성
  @ApiBearerAuth()
  @Post('posts')
  @ApiOperation({ summary: '게시글 작성' })
  create(@CurrentUser() user: JwtPayload, @Body() dto: CreateCommunityPostDto) {
    return this.communityService.create(user.sub, dto);
  }

  // COMM-05: 게시글 수정
  @ApiBearerAuth()
  @Patch('posts/:id')
  @ApiOperation({ summary: '게시글 수정' })
  update(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: UpdateCommunityPostDto,
  ) {
    return this.communityService.update(user.sub, user.role as UserRole, id, dto);
  }

  // COMM-06: 게시글 삭제
  @ApiBearerAuth()
  @Delete('posts/:id')
  @ApiOperation({ summary: '게시글 삭제' })
  remove(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.communityService.remove(user.sub, user.role as UserRole, id);
  }
}
