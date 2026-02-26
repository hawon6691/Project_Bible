import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { UploadSecurityService } from '../upload/upload-security.service';
import { UploadImageDto } from './dto/upload-image.dto';
import { ImageAsset, ImageProcessingStatus } from './entities/image-asset.entity';
import { ImageVariant, ImageVariantType } from './entities/image-variant.entity';

@Injectable()
export class ImageService {
  private static readonly MAX_FILE_SIZE = 10 * 1024 * 1024;
  private static readonly ALLOWED_MIME_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/gif']);

  constructor(
    @InjectRepository(ImageAsset)
    private imageAssetRepository: Repository<ImageAsset>,
    @InjectRepository(ImageVariant)
    private imageVariantRepository: Repository<ImageVariant>,
    private readonly uploadSecurityService: UploadSecurityService,
  ) {}

  // Image 업로드 후 원본/변환본 메타데이터를 생성한다.
  async upload(file: Express.Multer.File | undefined, dto: UploadImageDto, userId: number) {
    await this.validateFile(file);
    const imageFile = file as Express.Multer.File;

    const fileExtension = this.getExtension(imageFile.originalname);
    const token = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
    const storedFilename = `${token}.${fileExtension}`;

    const imageAsset = this.imageAssetRepository.create({
      uploadedByUserId: userId,
      originalFilename: imageFile.originalname,
      storedFilename,
      originalUrl: `/uploads/original/${storedFilename}`,
      mimeType: imageFile.mimetype,
      size: imageFile.size,
      category: dto.category,
      processingStatus: ImageProcessingStatus.PROCESSING,
    });

    const savedImage = await this.imageAssetRepository.save(imageAsset);

    const variants = await this.createDefaultVariants(savedImage.id, token, imageFile.size);

    savedImage.processingStatus = ImageProcessingStatus.COMPLETED;
    const completedImage = await this.imageAssetRepository.save(savedImage);

    return {
      id: completedImage.id,
      originalUrl: completedImage.originalUrl,
      variants: variants.map((variant) => this.toVariantDetail(variant)),
      processingStatus: completedImage.processingStatus,
    };
  }

  async getVariants(imageId: number) {
    await this.ensureImage(imageId);

    const variants = await this.imageVariantRepository.find({
      where: { imageId },
      order: { id: 'ASC' },
    });

    return variants.map((variant) => this.toVariantDetail(variant));
  }

  async remove(imageId: number) {
    await this.ensureImage(imageId);

    await this.imageVariantRepository.softDelete({ imageId });
    await this.imageAssetRepository.softDelete({ id: imageId });

    return { success: true, message: '이미지가 삭제되었습니다.' };
  }

  private async createDefaultVariants(imageId: number, token: string, originalSize: number) {
    const variantDefinitions = [
      { type: ImageVariantType.THUMBNAIL, path: 'thumb', width: 200, height: 200, sizeRatio: 0.12 },
      { type: ImageVariantType.MEDIUM, path: 'medium', width: 600, height: 600, sizeRatio: 0.35 },
      { type: ImageVariantType.LARGE, path: 'large', width: 1200, height: 1200, sizeRatio: 0.65 },
    ];

    const entities = variantDefinitions.map((definition) =>
      this.imageVariantRepository.create({
        imageId,
        type: definition.type,
        url: `/uploads/${definition.path}/${token}.webp`,
        width: definition.width,
        height: definition.height,
        format: 'webp',
        size: Math.max(1, Math.floor(originalSize * definition.sizeRatio)),
      }),
    );

    return this.imageVariantRepository.save(entities);
  }

  private async ensureImage(imageId: number) {
    const image = await this.imageAssetRepository.findOne({ where: { id: imageId } });
    if (!image) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return image;
  }

  private async validateFile(file: Express.Multer.File | undefined) {
    if (!file) {
      throw new BusinessException('FILE_UPLOAD_FAILED', HttpStatus.BAD_REQUEST, '업로드할 파일이 필요합니다.');
    }

    if (file.size > ImageService.MAX_FILE_SIZE) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '최대 10MB 파일만 업로드할 수 있습니다.');
    }

    if (!ImageService.ALLOWED_MIME_TYPES.has(file.mimetype)) {
      throw new BusinessException('FILE_TYPE_NOT_ALLOWED', HttpStatus.BAD_REQUEST);
    }

    await this.uploadSecurityService.validateFile(file);
  }

  private getExtension(filename: string) {
    const parts = filename.split('.');
    return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : 'bin';
  }

  private toVariantDetail(variant: ImageVariant) {
    return {
      id: variant.id,
      type: variant.type,
      url: variant.url,
      width: variant.width,
      height: variant.height,
      format: variant.format,
      size: variant.size,
      createdAt: variant.createdAt,
    };
  }
}
