import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Post,
  Query,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { ApiBearerAuth, ApiConsumes, ApiOperation, ApiTags } from '@nestjs/swagger';
import { FileInterceptor } from '@nestjs/platform-express';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { CreateShortformCommentDto } from './dto/create-shortform-comment.dto';
import { CreateShortformDto } from './dto/create-shortform.dto';
import { ShortformCommentQueryDto } from './dto/shortform-comment-query.dto';
import { ShortformFeedQueryDto } from './dto/shortform-feed-query.dto';
import { ShortformRankingQueryDto } from './dto/shortform-ranking-query.dto';
import { VideoService } from './video.service';

@ApiTags('Shortform')
@Controller('shortforms')
export class VideoController {
  constructor(private readonly videoService: VideoService) {}

  @ApiBearerAuth()
  @Post()
  @UseInterceptors(FileInterceptor('video'))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({ summary: '숏폼 업로드' })
  createShortform(
    @CurrentUser() user: JwtPayload,
    @UploadedFile() video: Express.Multer.File,
    @Body() dto: CreateShortformDto,
  ) {
    return this.videoService.createShortform(user.sub, video, dto);
  }

  @Public()
  @Get()
  @ApiOperation({ summary: '숏폼 피드 조회' })
  getFeed(@Query() query: ShortformFeedQueryDto, @CurrentUser() viewer?: JwtPayload) {
    return this.videoService.getFeed(query, viewer?.sub);
  }

  @Public()
  @Get(':id')
  @ApiOperation({ summary: '숏폼 상세 조회' })
  getDetail(@Param('id', ParseIntPipe) id: number, @CurrentUser() viewer?: JwtPayload) {
    return this.videoService.getShortformDetail(id, viewer?.sub);
  }

  @ApiBearerAuth()
  @Post(':id/like')
  @ApiOperation({ summary: '숏폼 좋아요 토글' })
  toggleLike(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.videoService.toggleLike(user.sub, id);
  }

  @ApiBearerAuth()
  @Post(':id/comments')
  @ApiOperation({ summary: '숏폼 댓글 작성' })
  createComment(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: CreateShortformCommentDto,
  ) {
    return this.videoService.createComment(user.sub, id, dto);
  }

  @Public()
  @Get(':id/comments')
  @ApiOperation({ summary: '숏폼 댓글 목록 조회' })
  getComments(@Param('id', ParseIntPipe) id: number, @Query() query: ShortformCommentQueryDto) {
    return this.videoService.getComments(id, query);
  }

  @Public()
  @Get('ranking/list')
  @ApiOperation({ summary: '숏폼 랭킹 조회' })
  getRanking(@Query() query: ShortformRankingQueryDto, @CurrentUser() viewer?: JwtPayload) {
    return this.videoService.getRanking(query, viewer?.sub);
  }

  @ApiBearerAuth()
  @Delete(':id')
  @ApiOperation({ summary: '숏폼 삭제' })
  removeShortform(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.videoService.removeShortform(user.sub, id);
  }

  @Public()
  @Get('user/:userId')
  @ApiOperation({ summary: '특정 유저 숏폼 목록 조회' })
  getUserShortforms(
    @Param('userId', ParseIntPipe) userId: number,
    @Query() query: ShortformFeedQueryDto,
    @CurrentUser() viewer?: JwtPayload,
  ) {
    return this.videoService.getUserShortforms(userId, query, viewer?.sub);
  }
}
