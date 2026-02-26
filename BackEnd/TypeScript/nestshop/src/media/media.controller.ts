import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Post,
  UploadedFiles,
  UseInterceptors,
} from '@nestjs/common';
import { ApiBearerAuth, ApiConsumes, ApiOperation, ApiTags } from '@nestjs/swagger';
import { FilesInterceptor } from '@nestjs/platform-express';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { CreatePresignedUrlDto } from './dto/create-presigned-url.dto';
import { UploadMediaDto } from './dto/upload-media.dto';
import { MediaService } from './media.service';

@ApiTags('Media')
@Controller('media')
export class MediaController {
  constructor(private readonly mediaService: MediaService) {}

  @ApiBearerAuth()
  @Post('upload')
  @UseInterceptors(FilesInterceptor('files'))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({ summary: '파일 업로드' })
  upload(
    @CurrentUser() user: JwtPayload,
    @UploadedFiles() files: Express.Multer.File[],
    @Body() dto: UploadMediaDto,
  ) {
    return this.mediaService.upload(user.sub, files, dto);
  }

  @ApiBearerAuth()
  @Post('presigned-url')
  @ApiOperation({ summary: 'Presigned URL 발급' })
  createPresignedUrl(@CurrentUser() user: JwtPayload, @Body() dto: CreatePresignedUrlDto) {
    return this.mediaService.createPresignedUrl(user.sub, dto);
  }

  @Public()
  @Get('stream/:id')
  @ApiOperation({ summary: '파일 스트리밍 정보 조회' })
  getStreamInfo(@Param('id', ParseIntPipe) id: number) {
    return this.mediaService.getStreamInfo(id);
  }

  @ApiBearerAuth()
  @Delete(':id')
  @ApiOperation({ summary: '파일 삭제' })
  remove(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.mediaService.remove(user.sub, id);
  }

  @Public()
  @Get(':id/metadata')
  @ApiOperation({ summary: '파일 메타데이터 조회' })
  getMetadata(@Param('id', ParseIntPipe) id: number) {
    return this.mediaService.getMetadata(id);
  }
}
