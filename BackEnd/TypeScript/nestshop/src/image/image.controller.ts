import {
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Post,
  UploadedFile,
  UseInterceptors,
  Body,
} from '@nestjs/common';
import { ApiBearerAuth, ApiConsumes, ApiOperation, ApiTags } from '@nestjs/swagger';
import { FileInterceptor } from '@nestjs/platform-express';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { UploadImageDto } from './dto/upload-image.dto';
import { ImageService } from './image.service';

@ApiTags('Image')
@Controller('images')
export class ImageController {
  constructor(private readonly imageService: ImageService) {}

  // IMG-01: 이미지 업로드 + 최적화 메타데이터 생성
  @ApiBearerAuth()
  @Roles(UserRole.USER, UserRole.ADMIN)
  @Post('upload')
  @UseInterceptors(FileInterceptor('file'))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({ summary: '이미지 업로드 + 변환 메타데이터 생성' })
  upload(
    @CurrentUser() user: JwtPayload,
    @UploadedFile() file: Express.Multer.File,
    @Body() dto: UploadImageDto,
  ) {
    return this.imageService.upload(file, dto, user.sub);
  }

  // IMG-02: 변환본 조회
  @Public()
  @Get(':id/variants')
  @ApiOperation({ summary: '이미지 변환본 조회' })
  getVariants(@Param('id', ParseIntPipe) id: number) {
    return this.imageService.getVariants(id);
  }

  // IMG-03: 관리자 이미지 삭제
  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Delete(':id')
  @ApiOperation({ summary: '이미지 삭제 (Admin)' })
  remove(@Param('id', ParseIntPipe) id: number) {
    return this.imageService.remove(id);
  }
}
