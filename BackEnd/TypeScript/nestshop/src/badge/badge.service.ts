import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { User } from '../user/entities/user.entity';
import { BadgeConditionDto, CreateBadgeDto } from './dto/create-badge.dto';
import { GrantBadgeDto } from './dto/grant-badge.dto';
import { UpdateBadgeDto } from './dto/update-badge.dto';
import { Badge, BadgeType } from './entities/badge.entity';
import { UserBadge } from './entities/user-badge.entity';

@Injectable()
export class BadgeService {
  constructor(
    @InjectRepository(Badge)
    private badgeRepository: Repository<Badge>,
    @InjectRepository(UserBadge)
    private userBadgeRepository: Repository<UserBadge>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  async getAllBadges() {
    const badges = await this.badgeRepository.find({ order: { id: 'ASC' } });
    const counts = await this.getHolderCountMap();

    return badges.map((badge) => this.toBadgeDetail(badge, counts.get(badge.id) ?? 0));
  }

  async getMyBadges(userId: number) {
    return this.getUserBadges(userId);
  }

  async getUserBadges(userId: number) {
    await this.ensureUser(userId);

    const userBadges = await this.userBadgeRepository.find({
      where: { userId },
      relations: { badge: true },
      order: { grantedAt: 'DESC' },
    });

    return userBadges.map((item) => this.toUserBadgeDetail(item));
  }

  async create(dto: CreateBadgeDto) {
    await this.ensureBadgeNameUnique(dto.name);
    const condition = this.toConditionObject(dto.condition);
    this.validateCondition(dto.type, condition);

    const badge = this.badgeRepository.create({
      name: dto.name,
      description: dto.description,
      iconUrl: dto.iconUrl,
      type: dto.type,
      condition,
      rarity: dto.rarity,
    });

    const saved = await this.badgeRepository.save(badge);
    return this.toBadgeDetail(saved, 0);
  }

  async update(id: number, dto: UpdateBadgeDto) {
    const badge = await this.ensureBadge(id);

    if (dto.name && dto.name !== badge.name) {
      await this.ensureBadgeNameUnique(dto.name);
      badge.name = dto.name;
    }

    if (dto.description !== undefined) badge.description = dto.description;
    if (dto.iconUrl !== undefined) badge.iconUrl = dto.iconUrl;
    if (dto.type !== undefined) badge.type = dto.type;
    if (dto.rarity !== undefined) badge.rarity = dto.rarity;

    if (dto.condition !== undefined) {
      const condition = this.toConditionObject(dto.condition);
      this.validateCondition(dto.type ?? badge.type, condition);
      badge.condition = condition;
    } else {
      this.validateCondition(dto.type ?? badge.type, badge.condition);
    }

    const saved = await this.badgeRepository.save(badge);
    const holderCount = await this.userBadgeRepository.count({ where: { badgeId: saved.id } });

    return this.toBadgeDetail(saved, holderCount);
  }

  async remove(id: number) {
    await this.ensureBadge(id);

    await this.userBadgeRepository.softDelete({ badgeId: id });
    await this.badgeRepository.softDelete({ id });

    return { success: true, message: '배지가 삭제되었습니다.' };
  }

  async grant(badgeId: number, dto: GrantBadgeDto, adminId: number) {
    await this.ensureBadge(badgeId);
    await this.ensureUser(dto.userId);

    const alreadyGranted = await this.userBadgeRepository.findOne({
      where: { badgeId, userId: dto.userId },
    });

    if (alreadyGranted) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.CONFLICT, '이미 부여된 배지입니다.');
    }

    const userBadge = this.userBadgeRepository.create({
      badgeId,
      userId: dto.userId,
      grantedByAdminId: adminId,
      reason: dto.reason ?? null,
      grantedAt: new Date(),
    });

    const saved = await this.userBadgeRepository.save(userBadge);
    const withBadge = await this.userBadgeRepository.findOne({
      where: { id: saved.id },
      relations: { badge: true },
    });

    return this.toUserBadgeDetail(withBadge as UserBadge);
  }

  async revoke(badgeId: number, userId: number) {
    const target = await this.userBadgeRepository.findOne({ where: { badgeId, userId } });
    if (!target) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.userBadgeRepository.softDelete({ id: target.id });
    return { success: true, message: '배지가 회수되었습니다.' };
  }

  private async ensureBadge(id: number) {
    const badge = await this.badgeRepository.findOne({ where: { id } });
    if (!badge) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return badge;
  }

  private async ensureUser(id: number) {
    const user = await this.userRepository.findOne({ where: { id } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return user;
  }

  private async ensureBadgeNameUnique(name: string) {
    const exists = await this.badgeRepository.findOne({ where: { name } });
    if (exists) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.CONFLICT, '이미 존재하는 배지명입니다.');
    }
  }

  private validateCondition(type: BadgeType, condition: Record<string, unknown> | null) {
    if (type === BadgeType.AUTO && !condition) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, 'AUTO 배지는 condition이 필요합니다.');
    }
  }

  private toConditionObject(condition?: BadgeConditionDto | null): Record<string, unknown> | null {
    if (!condition) {
      return null;
    }

    return {
      metric: condition.metric,
      threshold: condition.threshold,
    };
  }

  private async getHolderCountMap() {
    const rows = await this.userBadgeRepository
      .createQueryBuilder('userBadge')
      .select('userBadge.badgeId', 'badgeId')
      .addSelect('COUNT(*)', 'count')
      .groupBy('userBadge.badgeId')
      .getRawMany<{ badgeId: string; count: string }>();

    return new Map(rows.map((row) => [Number(row.badgeId), Number(row.count)]));
  }

  private toBadgeDetail(badge: Badge, holderCount: number) {
    return {
      id: badge.id,
      name: badge.name,
      description: badge.description,
      iconUrl: badge.iconUrl,
      type: badge.type,
      condition: badge.condition,
      rarity: badge.rarity,
      holderCount,
      createdAt: badge.createdAt,
      updatedAt: badge.updatedAt,
    };
  }

  private toUserBadgeDetail(userBadge: UserBadge) {
    return {
      id: userBadge.id,
      userId: userBadge.userId,
      badgeId: userBadge.badgeId,
      grantedByAdminId: userBadge.grantedByAdminId,
      reason: userBadge.reason,
      grantedAt: userBadge.grantedAt,
      badge: userBadge.badge
        ? {
            id: userBadge.badge.id,
            name: userBadge.badge.name,
            description: userBadge.badge.description,
            iconUrl: userBadge.badge.iconUrl,
            type: userBadge.badge.type,
            rarity: userBadge.badge.rarity,
          }
        : null,
    };
  }
}
