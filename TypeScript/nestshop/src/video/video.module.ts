import { Module } from '@nestjs/common';
import { BullModule } from '@nestjs/bull';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../user/entities/user.entity';
import { VideoController } from './video.controller';
import { VideoProcessor } from './video.processor';
import { VideoService } from './video.service';
import { ShortformComment } from './entities/shortform-comment.entity';
import { ShortformLike } from './entities/shortform-like.entity';
import { ShortformProduct } from './entities/shortform-product.entity';
import { Shortform } from './entities/shortform.entity';
import { UploadSecurityModule } from '../upload/upload-security.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([Shortform, ShortformLike, ShortformComment, ShortformProduct, User]),
    BullModule.registerQueue({ name: 'video-transcode' }),
    UploadSecurityModule,
  ],
  controllers: [VideoController],
  providers: [VideoService, VideoProcessor],
  exports: [VideoService],
})
export class VideoModule {}
