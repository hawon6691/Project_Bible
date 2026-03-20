import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../user/entities/user.entity';
import { FriendController } from './friend.controller';
import { FriendService } from './friend.service';
import { FriendActivity } from './entities/friend-activity.entity';
import { FriendBlock } from './entities/friend-block.entity';
import { Friendship } from './entities/friendship.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Friendship, FriendBlock, FriendActivity, User])],
  controllers: [FriendController],
  providers: [FriendService],
  exports: [FriendService],
})
export class FriendModule {}
