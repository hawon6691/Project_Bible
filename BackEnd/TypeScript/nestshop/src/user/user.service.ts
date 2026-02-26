import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User, UserStatus } from './entities/user.entity';
import { UpdateUserDto } from './dto/update-user.dto';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { hashPassword } from '../common/utils/hash.util';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';

@Injectable()
export class UserService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  // ─── USER-01: 내 정보 조회 ───
  async getMe(userId: number) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return this.toUserResponse(user);
  }

  // ─── USER-02: 내 정보 수정 ───
  async updateMe(userId: number, dto: UpdateUserDto) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.name) user.name = dto.name;
    if (dto.phone) user.phone = dto.phone.replace(/[\s-]/g, '');
    if (dto.password) user.password = await hashPassword(dto.password);

    const saved = await this.userRepository.save(user);
    return this.toUserResponse(saved);
  }

  // ─── USER-03: 회원 탈퇴 ───
  async deleteMe(userId: number) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.userRepository.softDelete(userId);
    return { message: '회원 탈퇴가 완료되었습니다.' };
  }

  // ─── USER-04: 회원 목록 조회 (Admin) ───
  async findAll(query: PaginationRequestDto) {
    const [items, totalItems] = await this.userRepository.findAndCount({
      skip: query.skip,
      take: query.limit,
      order: { createdAt: 'DESC' },
    });

    return new PaginationResponseDto(
      items.map((u) => this.toUserResponse(u)),
      totalItems,
      query.page,
      query.limit,
    );
  }

  // ─── USER-05: 회원 상태 변경 (Admin) ───
  async updateStatus(userId: number, status: UserStatus) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    user.status = status;
    const saved = await this.userRepository.save(user);
    return this.toUserResponse(saved);
  }

  // ─── USER-06: 프로필 조회 ───
  async getProfile(userId: number) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return {
      id: user.id,
      nickname: user.nickname,
      bio: user.bio,
      profileImageUrl: user.profileImageUrl,
      createdAt: user.createdAt,
    };
  }

  // ─── USER-07, 08: 닉네임/소개글 수정 ───
  async updateProfile(userId: number, dto: UpdateProfileDto) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.nickname !== undefined) {
      // 닉네임 중복 확인
      const existing = await this.userRepository.findOne({
        where: { nickname: dto.nickname },
      });
      if (existing && existing.id !== userId) {
        throw new BusinessException('USER_NICKNAME_DUPLICATE', HttpStatus.CONFLICT);
      }
      user.nickname = dto.nickname;
    }

    if (dto.bio !== undefined) {
      user.bio = dto.bio || null;
    }

    const saved = await this.userRepository.save(user);
    return {
      id: saved.id,
      nickname: saved.nickname,
      bio: saved.bio,
      profileImageUrl: saved.profileImageUrl,
    };
  }

  // ─── USER-09: 프로필 이미지 관리 ───
  async updateProfileImage(userId: number, imageUrl: string | null) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    user.profileImageUrl = imageUrl;
    const saved = await this.userRepository.save(user);

    return {
      id: saved.id,
      profileImageUrl: saved.profileImageUrl,
    };
  }

  // ─── 헬퍼: 응답 변환 ───
  private toUserResponse(user: User) {
    return {
      id: user.id,
      email: user.email,
      name: user.name,
      phone: user.phone,
      role: user.role,
      status: user.status,
      emailVerified: user.emailVerified,
      nickname: user.nickname,
      bio: user.bio,
      profileImageUrl: user.profileImageUrl,
      point: user.point,
      createdAt: user.createdAt,
    };
  }
}
