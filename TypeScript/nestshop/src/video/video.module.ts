import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../user/entities/user.entity';
import { VideoController } from './video.controller';
import { VideoService } from './video.service';
import { ShortformComment } from './entities/shortform-comment.entity';
import { ShortformLike } from './entities/shortform-like.entity';
import { ShortformProduct } from './entities/shortform-product.entity';
import { Shortform } from './entities/shortform.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Shortform, ShortformLike, ShortformComment, ShortformProduct, User])],
  controllers: [VideoController],
  providers: [VideoService],
  exports: [VideoService],
})
export class VideoModule {}
