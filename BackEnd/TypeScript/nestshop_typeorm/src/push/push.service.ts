import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { RegisterPushSubscriptionDto } from './dto/register-push-subscription.dto';
import { UnregisterPushSubscriptionDto } from './dto/unregister-push-subscription.dto';
import { UpdatePushPreferenceDto } from './dto/update-push-preference.dto';
import { PushPreference } from './entities/push-preference.entity';
import { PushSubscription } from './entities/push-subscription.entity';

@Injectable()
export class PushService {
  constructor(
    @InjectRepository(PushSubscription)
    private pushSubscriptionRepository: Repository<PushSubscription>,
    @InjectRepository(PushPreference)
    private pushPreferenceRepository: Repository<PushPreference>,
  ) {}

  // PUSH-01: 푸시 구독 등록
  async registerSubscription(userId: number, dto: RegisterPushSubscriptionDto) {
    const existing = await this.pushSubscriptionRepository.findOne({
      where: { userId, endpoint: dto.endpoint },
    });

    if (existing) {
      existing.p256dhKey = dto.p256dhKey;
      existing.authKey = dto.authKey;
      existing.expirationTime = dto.expirationTime ?? null;
      existing.isActive = true;
      const saved = await this.pushSubscriptionRepository.save(existing);
      return this.toSubscriptionDetail(saved);
    }

    const created = this.pushSubscriptionRepository.create({
      userId,
      endpoint: dto.endpoint,
      p256dhKey: dto.p256dhKey,
      authKey: dto.authKey,
      expirationTime: dto.expirationTime ?? null,
      isActive: true,
    });

    const saved = await this.pushSubscriptionRepository.save(created);
    return this.toSubscriptionDetail(saved);
  }

  // PUSH-02: 푸시 구독 해제
  async unregisterSubscription(userId: number, dto: UnregisterPushSubscriptionDto) {
    const existing = await this.pushSubscriptionRepository.findOne({
      where: { userId, endpoint: dto.endpoint },
    });

    if (!existing) {
      return { success: true, message: '이미 해제되었거나 존재하지 않는 구독입니다.' };
    }

    existing.isActive = false;
    await this.pushSubscriptionRepository.save(existing);
    return { success: true, message: '푸시 구독이 해제되었습니다.' };
  }

  // 현재 활성 구독 목록 조회
  async getMySubscriptions(userId: number) {
    const items = await this.pushSubscriptionRepository.find({
      where: { userId, isActive: true },
      order: { createdAt: 'DESC' },
    });

    return items.map((item) => this.toSubscriptionDetail(item));
  }

  // PUSH-07: 알림 설정 조회
  async getPreference(userId: number) {
    let pref = await this.pushPreferenceRepository.findOne({ where: { userId } });

    if (!pref) {
      pref = this.pushPreferenceRepository.create({
        userId,
        priceAlertEnabled: true,
        orderStatusEnabled: true,
        chatMessageEnabled: true,
        dealEnabled: true,
      });
      pref = await this.pushPreferenceRepository.save(pref);
    }

    return this.toPreferenceDetail(pref);
  }

  // PUSH-07: 알림 설정 변경
  async updatePreference(userId: number, dto: UpdatePushPreferenceDto) {
    let pref = await this.pushPreferenceRepository.findOne({ where: { userId } });

    if (!pref) {
      pref = this.pushPreferenceRepository.create({
        userId,
        priceAlertEnabled: true,
        orderStatusEnabled: true,
        chatMessageEnabled: true,
        dealEnabled: true,
      });
    }

    if (dto.priceAlertEnabled !== undefined) pref.priceAlertEnabled = dto.priceAlertEnabled;
    if (dto.orderStatusEnabled !== undefined) pref.orderStatusEnabled = dto.orderStatusEnabled;
    if (dto.chatMessageEnabled !== undefined) pref.chatMessageEnabled = dto.chatMessageEnabled;
    if (dto.dealEnabled !== undefined) pref.dealEnabled = dto.dealEnabled;

    const saved = await this.pushPreferenceRepository.save(pref);
    return this.toPreferenceDetail(saved);
  }

  private toSubscriptionDetail(item: PushSubscription) {
    return {
      id: item.id,
      endpoint: item.endpoint,
      expirationTime: item.expirationTime,
      isActive: item.isActive,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    };
  }

  private toPreferenceDetail(pref: PushPreference) {
    return {
      id: pref.id,
      userId: pref.userId,
      priceAlertEnabled: pref.priceAlertEnabled,
      orderStatusEnabled: pref.orderStatusEnabled,
      chatMessageEnabled: pref.chatMessageEnabled,
      dealEnabled: pref.dealEnabled,
      createdAt: pref.createdAt,
      updatedAt: pref.updatedAt,
    };
  }
}
