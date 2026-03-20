import { IsEnum } from 'class-validator';
import { ImageCategory } from '../entities/image-asset.entity';

export class UploadImageDto {
  @IsEnum(ImageCategory)
  category: ImageCategory;
}
