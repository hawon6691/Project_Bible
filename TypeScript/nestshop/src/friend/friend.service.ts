import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { User } from '../user/entities/user.entity';
import { FriendPaginationQueryDto } from './dto/friend-pagination-query.dto';
import { FriendActivity } from './entities/friend-activity.entity';
import { FriendBlock } from './entities/friend-block.entity';
import { Friendship, FriendshipStatus } from './entities/friendship.entity';

@Injectable()
export class FriendService {
  constructor(
    @InjectRepository(Friendship)
    private friendshipRepository: Repository<Friendship>,
    @InjectRepository(FriendBlock)
    private friendBlockRepository: Repository<FriendBlock>,
    @InjectRepository(FriendActivity)
    private friendActivityRepository: Repository<FriendActivity>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  async requestFriend(userId: number, targetUserId: number) {
    await this.ensureUsers(userId, targetUserId);
    this.ensureNotSelf(userId, targetUserId);
    await this.ensureNotBlocked(userId, targetUserId);

    const existing = await this.findFriendshipPair(userId, targetUserId);
    if (existing && existing.status === FriendshipStatus.ACCEPTED) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.CONFLICT, '이미 친구입니다.');
    }

    if (existing && existing.status === FriendshipStatus.PENDING) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.CONFLICT, '이미 친구 요청이 존재합니다.');
    }

    if (existing) {
      existing.requesterId = userId;
      existing.addresseeId = targetUserId;
      existing.status = FriendshipStatus.PENDING;
      existing.respondedAt = null;
      await this.friendshipRepository.save(existing);
    } else {
      const friendship = this.friendshipRepository.create({
        requesterId: userId,
        addresseeId: targetUserId,
        status: FriendshipStatus.PENDING,
        respondedAt: null,
      });
      await this.friendshipRepository.save(friendship);
    }

    return { success: true, message: '친구 요청을 보냈습니다.' };
  }

  async acceptRequest(userId: number, friendshipId: number) {
    const friendship = await this.friendshipRepository.findOne({ where: { id: friendshipId } });
    if (!friendship) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (friendship.addresseeId !== userId) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }

    if (friendship.status !== FriendshipStatus.PENDING) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '대기중인 요청만 수락할 수 있습니다.');
    }

    friendship.status = FriendshipStatus.ACCEPTED;
    friendship.respondedAt = new Date();
    await this.friendshipRepository.save(friendship);

    await this.createActivity(friendship.requesterId, 'FRIEND_ACCEPTED', `유저 ${userId}님과 친구가 되었습니다.`, {
      friendUserId: userId,
    });
    await this.createActivity(userId, 'FRIEND_ACCEPTED', `유저 ${friendship.requesterId}님과 친구가 되었습니다.`, {
      friendUserId: friendship.requesterId,
    });

    return { success: true, message: '친구 요청을 수락했습니다.' };
  }

  async rejectRequest(userId: number, friendshipId: number) {
    const friendship = await this.friendshipRepository.findOne({ where: { id: friendshipId } });
    if (!friendship) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (friendship.addresseeId !== userId) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }

    if (friendship.status !== FriendshipStatus.PENDING) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '대기중인 요청만 거절할 수 있습니다.');
    }

    friendship.status = FriendshipStatus.REJECTED;
    friendship.respondedAt = new Date();
    await this.friendshipRepository.save(friendship);

    return { success: true, message: '친구 요청을 거절했습니다.' };
  }

  async getFriends(userId: number, query: FriendPaginationQueryDto) {
    const [rows, total] = await this.friendshipRepository.findAndCount({
      where: [
        { requesterId: userId, status: FriendshipStatus.ACCEPTED },
        { addresseeId: userId, status: FriendshipStatus.ACCEPTED },
      ],
      order: { updatedAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const friendIds = rows.map((row) => (row.requesterId === userId ? row.addresseeId : row.requesterId));
    const users = friendIds.length ? await this.userRepository.find({ where: { id: In(friendIds) } }) : [];
    const userMap = new Map(users.map((item) => [item.id, item]));

    const items = rows.map((row) => {
      const friendId = row.requesterId === userId ? row.addresseeId : row.requesterId;
      const friend = userMap.get(friendId);

      return {
        friendshipId: row.id,
        userId: friendId,
        nickname: friend?.nickname ?? null,
        profileImageUrl: friend?.profileImageUrl ?? null,
        status: row.status,
        since: row.respondedAt,
      };
    });

    return new PaginationResponseDto(items, total, query.page, query.limit);
  }

  async getReceivedRequests(userId: number, query: FriendPaginationQueryDto) {
    return this.getRequestList('received', userId, query);
  }

  async getSentRequests(userId: number, query: FriendPaginationQueryDto) {
    return this.getRequestList('sent', userId, query);
  }

  async getFeed(userId: number, query: FriendPaginationQueryDto) {
    const friends = await this.friendshipRepository.find({
      where: [
        { requesterId: userId, status: FriendshipStatus.ACCEPTED },
        { addresseeId: userId, status: FriendshipStatus.ACCEPTED },
      ],
    });

    const friendIds = friends.map((row) => (row.requesterId === userId ? row.addresseeId : row.requesterId));

    if (!friendIds.length) {
      return new PaginationResponseDto([], 0, query.page, query.limit);
    }

    const [activities, total] = await this.friendActivityRepository.findAndCount({
      where: { userId: In(friendIds) },
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const users = await this.userRepository.find({ where: { id: In(friendIds) } });
    const userMap = new Map(users.map((item) => [item.id, item]));

    const items = activities.map((item) => ({
      id: item.id,
      userId: item.userId,
      nickname: userMap.get(item.userId)?.nickname ?? null,
      profileImageUrl: userMap.get(item.userId)?.profileImageUrl ?? null,
      type: item.type,
      message: item.message,
      metadata: item.metadata,
      createdAt: item.createdAt,
    }));

    return new PaginationResponseDto(items, total, query.page, query.limit);
  }

  async blockUser(userId: number, targetUserId: number) {
    await this.ensureUsers(userId, targetUserId);
    this.ensureNotSelf(userId, targetUserId);

    const existing = await this.friendBlockRepository.findOne({ where: { userId, blockedUserId: targetUserId } });
    if (existing) {
      return { success: true, message: '이미 차단한 사용자입니다.' };
    }

    const block = this.friendBlockRepository.create({ userId, blockedUserId: targetUserId });
    await this.friendBlockRepository.save(block);

    const friendship = await this.findFriendshipPair(userId, targetUserId);
    if (friendship) {
      await this.friendshipRepository.softDelete({ id: friendship.id });
    }

    return { success: true, message: '사용자를 차단했습니다.' };
  }

  async unblockUser(userId: number, targetUserId: number) {
    const block = await this.friendBlockRepository.findOne({ where: { userId, blockedUserId: targetUserId } });
    if (!block) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.friendBlockRepository.softDelete({ id: block.id });
    return { success: true, message: '차단을 해제했습니다.' };
  }

  async removeFriend(userId: number, targetUserId: number) {
    const friendship = await this.findFriendshipPair(userId, targetUserId);
    if (!friendship || friendship.status !== FriendshipStatus.ACCEPTED) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.friendshipRepository.softDelete({ id: friendship.id });
    return { success: true, message: '친구를 삭제했습니다.' };
  }

  private async getRequestList(
    type: 'received' | 'sent',
    userId: number,
    query: FriendPaginationQueryDto,
  ) {
    const where =
      type === 'received'
        ? { addresseeId: userId, status: FriendshipStatus.PENDING }
        : { requesterId: userId, status: FriendshipStatus.PENDING };

    const [rows, total] = await this.friendshipRepository.findAndCount({
      where,
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const targetIds = rows.map((row) => (type === 'received' ? row.requesterId : row.addresseeId));
    const users = targetIds.length ? await this.userRepository.find({ where: { id: In(targetIds) } }) : [];
    const userMap = new Map(users.map((item) => [item.id, item]));

    const items = rows.map((row) => {
      const targetId = type === 'received' ? row.requesterId : row.addresseeId;
      const target = userMap.get(targetId);
      return {
        friendshipId: row.id,
        userId: targetId,
        nickname: target?.nickname ?? null,
        profileImageUrl: target?.profileImageUrl ?? null,
        requestedAt: row.createdAt,
      };
    });

    return new PaginationResponseDto(items, total, query.page, query.limit);
  }

  private async findFriendshipPair(userId: number, targetUserId: number) {
    return this.friendshipRepository.findOne({
      where: [
        { requesterId: userId, addresseeId: targetUserId },
        { requesterId: targetUserId, addresseeId: userId },
      ],
    });
  }

  private async ensureUsers(userId: number, targetUserId: number) {
    const users = await this.userRepository.find({ where: { id: In([userId, targetUserId]) } });
    if (users.length !== 2) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
  }

  private ensureNotSelf(userId: number, targetUserId: number) {
    if (userId === targetUserId) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '자기 자신에게는 수행할 수 없습니다.');
    }
  }

  private async ensureNotBlocked(userId: number, targetUserId: number) {
    const block = await this.friendBlockRepository.findOne({
      where: [
        { userId, blockedUserId: targetUserId },
        { userId: targetUserId, blockedUserId: userId },
      ],
    });

    if (block) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '차단된 사용자와는 요청할 수 없습니다.');
    }
  }

  private async createActivity(userId: number, type: string, message: string, metadata: Record<string, unknown>) {
    const activity = this.friendActivityRepository.create({
      userId,
      type,
      message,
      metadata,
    });

    await this.friendActivityRepository.save(activity);
  }
}
