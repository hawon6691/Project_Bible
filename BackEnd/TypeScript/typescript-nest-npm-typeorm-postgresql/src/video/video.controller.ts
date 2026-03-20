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
import { diskStorage } from 'multer';
import { mkdirSync } from 'fs';
import { extname, join } from 'path';
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
  private static readonly ALLOWED_MIME = new Set(['video/mp4', 'video/webm', 'video/quicktime']);

  constructor(private readonly videoService: VideoService) {}

  @ApiBearerAuth()
  @Post()
  @UseInterceptors(
    FileInterceptor('video', {
      // FFmpeg 워커가 접근할 수 있도록 원본 파일을 디스크에 저장한다.
      storage: diskStorage({
        destination: (_req, _file, cb) => {
          const uploadDir = join(process.cwd(), 'uploads', 'shortforms', 'raw');
          mkdirSync(uploadDir, { recursive: true });
          cb(null, uploadDir);
        },
        filename: (_req, file, cb) => {
          const token = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
          const extension = extname(file.originalname || '').toLowerCase() || '.mp4';
          cb(null, `${token}${extension}`);
        },
      }),
      fileFilter: (_req, file, cb) => {
        if (!VideoController.ALLOWED_MIME.has(file.mimetype)) {
          return cb(new Error('허용되지 않은 영상 형식입니다.'), false);
        }
        return cb(null, true);
      },
      limits: {
        fileSize: 200 * 1024 * 1024,
      },
    }),
  )
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
  @Get(':id/transcode-status')
  @ApiOperation({ summary: '숏폼 트랜스코딩 상태 조회' })
  getTranscodeStatus(@Param('id', ParseIntPipe) id: number) {
    return this.videoService.getTranscodeStatus(id);
  }

  @ApiBearerAuth()
  @Post(':id/transcode/retry')
  @ApiOperation({ summary: '숏폼 트랜스코딩 재시도' })
  retryTranscode(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.videoService.retryTranscode(user.sub, id);
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
