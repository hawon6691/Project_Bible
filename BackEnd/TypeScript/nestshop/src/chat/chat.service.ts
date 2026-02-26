import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { CreateChatRoomDto } from './dto/create-chat-room.dto';
import { SendChatMessageDto } from './dto/send-chat-message.dto';
import { ChatMessage } from './entities/chat-message.entity';
import { ChatRoomMember } from './entities/chat-room-member.entity';
import { ChatRoom } from './entities/chat-room.entity';

@Injectable()
export class ChatService {
  constructor(
    @InjectRepository(ChatRoom)
    private chatRoomRepository: Repository<ChatRoom>,
    @InjectRepository(ChatRoomMember)
    private chatRoomMemberRepository: Repository<ChatRoomMember>,
    @InjectRepository(ChatMessage)
    private chatMessageRepository: Repository<ChatMessage>,
  ) {}

  // CHAT-01: 채팅방 목록 조회 (내가 속한 방)
  async findMyRooms(userId: number, query: PaginationRequestDto) {
    const memberships = await this.chatRoomMemberRepository.find({
      where: { userId },
      order: { joinedAt: 'DESC' },
    });

    const roomIds = memberships.map((m) => m.roomId);
    if (!roomIds.length) {
      return new PaginationResponseDto([], 0, query.page, query.limit);
    }

    const [rooms, totalItems] = await this.chatRoomRepository.findAndCount({
      where: { id: In(roomIds) },
      order: { updatedAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    return new PaginationResponseDto(
      rooms.map((room) => this.toRoomDetail(room)),
      totalItems,
      query.page,
      query.limit,
    );
  }

  // CHAT-02: 채팅방 생성
  async createRoom(userId: number, dto: CreateChatRoomDto) {
    const room = this.chatRoomRepository.create({
      name: dto.name,
      createdBy: userId,
      isPrivate: dto.isPrivate ?? true,
    });
    const savedRoom = await this.chatRoomRepository.save(room);

    const membership = this.chatRoomMemberRepository.create({
      roomId: savedRoom.id,
      userId,
      joinedAt: new Date(),
    });
    await this.chatRoomMemberRepository.save(membership);

    return this.toRoomDetail(savedRoom);
  }

  // 채팅방 입장(멤버 추가)
  async joinRoom(userId: number, roomId: number) {
    const room = await this.chatRoomRepository.findOne({ where: { id: roomId } });
    if (!room) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const existing = await this.chatRoomMemberRepository.findOne({ where: { roomId, userId } });
    if (!existing) {
      const membership = this.chatRoomMemberRepository.create({
        roomId,
        userId,
        joinedAt: new Date(),
      });
      await this.chatRoomMemberRepository.save(membership);
    }

    return this.toRoomDetail(room);
  }

  // CHAT-03: 메시지 목록 조회
  async findMessages(userId: number, roomId: number, query: PaginationRequestDto) {
    await this.ensureRoomMember(roomId, userId);

    const [items, totalItems] = await this.chatMessageRepository.findAndCount({
      where: { roomId },
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    return new PaginationResponseDto(
      items.reverse().map((message) => this.toMessageDetail(message)),
      totalItems,
      query.page,
      query.limit,
    );
  }

  // CHAT-04: 메시지 전송
  async sendMessage(userId: number, roomId: number, dto: SendChatMessageDto) {
    await this.ensureRoomMember(roomId, userId);

    const created = this.chatMessageRepository.create({
      roomId,
      senderId: userId,
      message: dto.message,
    });
    const saved = await this.chatMessageRepository.save(created);
    return this.toMessageDetail(saved);
  }

  private async ensureRoomMember(roomId: number, userId: number) {
    const membership = await this.chatRoomMemberRepository.findOne({ where: { roomId, userId } });
    if (!membership) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN, '채팅방 접근 권한이 없습니다.');
    }
  }

  private toRoomDetail(room: ChatRoom) {
    return {
      id: room.id,
      name: room.name,
      createdBy: room.createdBy,
      isPrivate: room.isPrivate,
      createdAt: room.createdAt,
      updatedAt: room.updatedAt,
    };
  }

  private toMessageDetail(message: ChatMessage) {
    return {
      id: message.id,
      roomId: message.roomId,
      senderId: message.senderId,
      message: message.message,
      createdAt: message.createdAt,
      updatedAt: message.updatedAt,
    };
  }
}
