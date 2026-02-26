import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ImageController } from './image.controller';
import { ImageService } from './image.service';
import { ImageAsset } from './entities/image-asset.entity';
import { ImageVariant } from './entities/image-variant.entity';
import { UploadSecurityModule } from '../upload/upload-security.module';

@Module({
  imports: [TypeOrmModule.forFeature([ImageAsset, ImageVariant]), UploadSecurityModule],
  controllers: [ImageController],
  providers: [ImageService],
  exports: [ImageService],
})
export class ImageModule {}
