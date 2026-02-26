import { Type } from 'class-transformer';
import { IsEnum, IsInt, Min } from 'class-validator';
import { MediaOwnerType } from '../entities/media-asset.entity';

export class UploadMediaDto {
  @IsEnum(MediaOwnerType)
  ownerType: MediaOwnerType;

  @Type(() => Number)
  @IsInt()
  @Min(1)
  ownerId: number;
}
