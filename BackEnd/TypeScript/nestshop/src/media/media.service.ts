import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { UploadSecurityService } from '../upload/upload-security.service';
import { CreatePresignedUrlDto } from './dto/create-presigned-url.dto';
import { UploadMediaDto } from './dto/upload-media.dto';
import { MediaAsset, MediaType } from './entities/media-asset.entity';

@Injectable()
export class MediaService {
  private static readonly MAX_FILE_SIZE = 500 * 1024 * 1024;

  constructor(
    @InjectRepository(MediaAsset)
    private mediaAssetRepository: Repository<MediaAsset>,
    private readonly uploadSecurityService: UploadSecurityService,
  ) {}

  // 업로드된 파일들을 미디어 메타데이터로 저장한다.
  async upload(userId: number, files: Express.Multer.File[] | undefined, dto: UploadMediaDto) {
    if (!files?.length) {
      throw new BusinessException('FILE_UPLOAD_FAILED', HttpStatus.BAD_REQUEST, '업로드할 파일이 필요합니다.');
    }

    const created: MediaAsset[] = [];

    for (const file of files) {
      await this.validateFile(file);

      const fileKey = this.generateFileKey(file.originalname);
      const type = this.resolveMediaType(file.mimetype);

      const entity = this.mediaAssetRepository.create({
        uploaderId: userId,
        ownerType: dto.ownerType,
        ownerId: dto.ownerId,
        originalName: file.originalname,
        fileKey,
        fileUrl: `/uploads/media/${fileKey}`,
        type,
        mime: file.mimetype,
        size: String(file.size),
        duration: type === MediaType.VIDEO || type === MediaType.AUDIO ? 0 : null,
        width: type === MediaType.IMAGE || type === MediaType.VIDEO ? 0 : null,
        height: type === MediaType.IMAGE || type === MediaType.VIDEO ? 0 : null,
      });

      created.push(entity);
    }

    const saved = await this.mediaAssetRepository.save(created);
    return saved.map((item) => this.toAttachmentResponse(item));
  }

  // 대용량 업로드를 위한 presigned URL을 모의 발급한다.
  createPresignedUrl(userId: number, dto: CreatePresignedUrlDto) {
    const token = Math.random().toString(36).slice(2, 12);
    const fileKey = `presigned/${userId}/${Date.now()}-${token}-${dto.fileName}`;

    return {
      uploadUrl: `https://example-storage.local/upload/${encodeURIComponent(fileKey)}`,
      fileKey,
      expiresInSec: 900,
    };
  }

  // 실제 스트리밍 파일 대신 메타데이터/원본 URL을 반환한다.
  async getStreamInfo(id: number) {
    const media = await this.ensureMedia(id);

    return {
      id: media.id,
      fileUrl: media.fileUrl,
      mime: media.mime,
      size: Number(media.size),
      duration: media.duration,
      resolution: media.width && media.height ? `${media.width}x${media.height}` : null,
    };
  }

  async getMetadata(id: number) {
    const media = await this.ensureMedia(id);

    return {
      id: media.id,
      mime: media.mime,
      size: Number(media.size),
      duration: media.duration,
      resolution: media.width && media.height ? `${media.width}x${media.height}` : null,
      ownerType: media.ownerType,
      ownerId: media.ownerId,
      uploadedAt: media.createdAt,
    };
  }

  async remove(userId: number, id: number) {
    const media = await this.ensureMedia(id);

    if (media.uploaderId !== userId) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }

    await this.mediaAssetRepository.softDelete({ id });
    return { success: true, message: '파일이 삭제되었습니다.' };
  }

  private async ensureMedia(id: number) {
    const media = await this.mediaAssetRepository.findOne({ where: { id } });
    if (!media) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return media;
  }

  private async validateFile(file: Express.Multer.File) {
    if (file.size > MediaService.MAX_FILE_SIZE) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '최대 500MB 파일만 업로드할 수 있습니다.');
    }

    await this.uploadSecurityService.validateFile(file);
  }

  private resolveMediaType(mime: string) {
    if (mime.startsWith('image/')) return MediaType.IMAGE;
    if (mime.startsWith('video/')) return MediaType.VIDEO;
    if (mime.startsWith('audio/')) return MediaType.AUDIO;
    return MediaType.DOCUMENT;
  }

  private generateFileKey(originalName: string) {
    const safeName = originalName.replace(/\s+/g, '-');
    return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}-${safeName}`;
  }

  private toAttachmentResponse(item: MediaAsset) {
    return {
      id: item.id,
      ownerType: item.ownerType,
      ownerId: item.ownerId,
      fileKey: item.fileKey,
      fileUrl: item.fileUrl,
      type: item.type,
      mime: item.mime,
      size: Number(item.size),
      duration: item.duration,
      resolution: item.width && item.height ? `${item.width}x${item.height}` : null,
      createdAt: item.createdAt,
    };
  }
}
