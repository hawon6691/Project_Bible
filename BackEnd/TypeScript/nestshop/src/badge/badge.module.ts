import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../user/entities/user.entity';
import { BadgeController } from './badge.controller';
import { BadgeService } from './badge.service';
import { Badge } from './entities/badge.entity';
import { UserBadge } from './entities/user-badge.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Badge, UserBadge, User])],
  controllers: [BadgeController],
  providers: [BadgeService],
  exports: [BadgeService],
})
export class BadgeModule {}
