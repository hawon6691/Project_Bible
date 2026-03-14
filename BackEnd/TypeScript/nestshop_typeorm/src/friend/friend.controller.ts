import { Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { FriendPaginationQueryDto } from './dto/friend-pagination-query.dto';
import { FriendService } from './friend.service';

@ApiTags('Friend')
@ApiBearerAuth()
@Controller('friends')
export class FriendController {
  constructor(private readonly friendService: FriendService) {}

  @Post('request/:userId')
  @ApiOperation({ summary: '친구 신청' })
  requestFriend(@CurrentUser() user: JwtPayload, @Param('userId', ParseIntPipe) userId: number) {
    return this.friendService.requestFriend(user.sub, userId);
  }

  @Patch('request/:friendshipId/accept')
  @ApiOperation({ summary: '친구 신청 수락' })
  acceptRequest(
    @CurrentUser() user: JwtPayload,
    @Param('friendshipId', ParseIntPipe) friendshipId: number,
  ) {
    return this.friendService.acceptRequest(user.sub, friendshipId);
  }

  @Patch('request/:friendshipId/reject')
  @ApiOperation({ summary: '친구 신청 거절' })
  rejectRequest(
    @CurrentUser() user: JwtPayload,
    @Param('friendshipId', ParseIntPipe) friendshipId: number,
  ) {
    return this.friendService.rejectRequest(user.sub, friendshipId);
  }

  @Get()
  @ApiOperation({ summary: '내 친구 목록 조회' })
  getFriends(@CurrentUser() user: JwtPayload, @Query() query: FriendPaginationQueryDto) {
    return this.friendService.getFriends(user.sub, query);
  }

  @Get('requests/received')
  @ApiOperation({ summary: '받은 친구 요청 목록' })
  getReceivedRequests(@CurrentUser() user: JwtPayload, @Query() query: FriendPaginationQueryDto) {
    return this.friendService.getReceivedRequests(user.sub, query);
  }

  @Get('requests/sent')
  @ApiOperation({ summary: '보낸 친구 요청 목록' })
  getSentRequests(@CurrentUser() user: JwtPayload, @Query() query: FriendPaginationQueryDto) {
    return this.friendService.getSentRequests(user.sub, query);
  }

  @Get('feed')
  @ApiOperation({ summary: '친구 활동 피드 조회' })
  getFeed(@CurrentUser() user: JwtPayload, @Query() query: FriendPaginationQueryDto) {
    return this.friendService.getFeed(user.sub, query);
  }

  @Post('block/:userId')
  @ApiOperation({ summary: '유저 차단' })
  blockUser(@CurrentUser() user: JwtPayload, @Param('userId', ParseIntPipe) userId: number) {
    return this.friendService.blockUser(user.sub, userId);
  }

  @Delete('block/:userId')
  @ApiOperation({ summary: '유저 차단 해제' })
  unblockUser(@CurrentUser() user: JwtPayload, @Param('userId', ParseIntPipe) userId: number) {
    return this.friendService.unblockUser(user.sub, userId);
  }

  @Delete(':userId')
  @ApiOperation({ summary: '친구 삭제' })
  removeFriend(@CurrentUser() user: JwtPayload, @Param('userId', ParseIntPipe) userId: number) {
    return this.friendService.removeFriend(user.sub, userId);
  }
}
