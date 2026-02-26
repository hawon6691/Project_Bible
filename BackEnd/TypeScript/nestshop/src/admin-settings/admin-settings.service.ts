import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CacheService } from '../common/cache/cache.service';
import { SetAllowedExtensionsDto } from './dto/set-allowed-extensions.dto';
import { UpdateReviewPolicyDto } from './dto/update-review-policy.dto';
import { UpdateUploadLimitsDto } from './dto/update-upload-limits.dto';
import { SystemSetting } from './entities/system-setting.entity';

@Injectable()
export class AdminSettingsService {
  private readonly defaultExtensions = ['jpg', 'jpeg', 'png', 'webp', 'gif', 'mp4', 'mp3', 'pdf'];
  private readonly defaultUploadLimits = { image: 5, video: 100, audio: 20 };
  private readonly defaultReviewPolicy = { maxImageCount: 10, pointAmount: 500 };

  constructor(
    @InjectRepository(SystemSetting)
    private systemSettingRepository: Repository<SystemSetting>,
    private readonly cacheService: CacheService,
  ) {}

  async getAllowedExtensions() {
    const setting = await this.getOrCreate('allowed_extensions', { extensions: this.defaultExtensions });
    return { extensions: this.normalizeExtensions(setting.settingValue.extensions as string[]) };
  }

  async setAllowedExtensions(dto: SetAllowedExtensionsDto) {
    const normalized = this.normalizeExtensions(dto.extensions);
    const setting = await this.upsert('allowed_extensions', { extensions: normalized });
    await this.cacheService.del('settings:allowed_extensions');
    return { extensions: this.normalizeExtensions(setting.settingValue.extensions as string[]) };
  }

  async getUploadLimits() {
    const setting = await this.getOrCreate('upload_limits', this.defaultUploadLimits);
    const value = setting.settingValue;

    return {
      image: Number(value.image ?? this.defaultUploadLimits.image),
      video: Number(value.video ?? this.defaultUploadLimits.video),
      audio: Number(value.audio ?? this.defaultUploadLimits.audio),
    };
  }

  async updateUploadLimits(dto: UpdateUploadLimitsDto) {
    const current = await this.getUploadLimits();

    const merged = {
      image: dto.image ?? current.image,
      video: dto.video ?? current.video,
      audio: dto.audio ?? current.audio,
    };

    const setting = await this.upsert('upload_limits', merged);
    const value = setting.settingValue;

    return {
      image: Number(value.image),
      video: Number(value.video),
      audio: Number(value.audio),
    };
  }

  async getReviewPolicy() {
    const setting = await this.getOrCreate('review_policy', this.defaultReviewPolicy);
    const value = setting.settingValue;

    return {
      maxImageCount: Number(value.maxImageCount ?? this.defaultReviewPolicy.maxImageCount),
      pointAmount: Number(value.pointAmount ?? this.defaultReviewPolicy.pointAmount),
    };
  }

  async updateReviewPolicy(dto: UpdateReviewPolicyDto) {
    const setting = await this.upsert('review_policy', {
      maxImageCount: dto.maxImageCount,
      pointAmount: dto.pointAmount,
    });

    const value = setting.settingValue;
    return {
      maxImageCount: Number(value.maxImageCount),
      pointAmount: Number(value.pointAmount),
    };
  }

  private async getOrCreate(key: string, defaultValue: Record<string, unknown>) {
    const existing = await this.systemSettingRepository.findOne({ where: { settingKey: key } });
    if (existing) {
      return existing;
    }

    const created = this.systemSettingRepository.create({
      settingKey: key,
      settingValue: defaultValue,
    });

    return this.systemSettingRepository.save(created);
  }

  private async upsert(key: string, value: Record<string, unknown>) {
    let existing = await this.systemSettingRepository.findOne({ where: { settingKey: key } });

    if (!existing) {
      existing = this.systemSettingRepository.create({ settingKey: key, settingValue: value });
    } else {
      existing.settingValue = value;
    }

    return this.systemSettingRepository.save(existing);
  }

  private normalizeExtensions(extensions: string[]) {
    const normalized = extensions.map((ext) => ext.trim().toLowerCase()).filter(Boolean);
    return [...new Set(normalized)].sort();
  }
}
