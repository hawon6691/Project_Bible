import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PriceEntry } from '../price/entities/price-entry.entity';
import { Product } from '../product/entities/product.entity';
import { User } from '../user/entities/user.entity';
import { AddPcBuildPartDto } from './dto/add-pc-build-part.dto';
import { CreateCompatibilityRuleDto } from './dto/create-compatibility-rule.dto';
import { CreatePcBuildDto } from './dto/create-pc-build.dto';
import { PcBuildListQueryDto } from './dto/pc-build-list-query.dto';
import { UpdateCompatibilityRuleDto } from './dto/update-compatibility-rule.dto';
import { UpdatePcBuildDto } from './dto/update-pc-build.dto';
import { PcBuildPart, PcPartType } from './entities/pc-build-part.entity';
import { PcBuild } from './entities/pc-build.entity';
import { PcCompatibilityRule } from './entities/pc-compatibility-rule.entity';

@Injectable()
export class PcBuilderService {
  private static readonly REQUIRED_PARTS: PcPartType[] = [
    PcPartType.CPU,
    PcPartType.MOTHERBOARD,
    PcPartType.RAM,
    PcPartType.GPU,
    PcPartType.SSD,
    PcPartType.PSU,
    PcPartType.CASE,
  ];

  constructor(
    @InjectRepository(PcBuild)
    private pcBuildRepository: Repository<PcBuild>,
    @InjectRepository(PcBuildPart)
    private pcBuildPartRepository: Repository<PcBuildPart>,
    @InjectRepository(PcCompatibilityRule)
    private pcCompatibilityRuleRepository: Repository<PcCompatibilityRule>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(PriceEntry)
    private priceEntryRepository: Repository<PriceEntry>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  async getMyBuilds(userId: number, query: PcBuildListQueryDto) {
    await this.ensureUser(userId);

    const [items, total] = await this.pcBuildRepository.findAndCount({
      where: { userId },
      order: { updatedAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const summaries = items.map((item) => this.toBuildSummary(item));
    return new PaginationResponseDto(summaries, total, query.page, query.limit);
  }

  async createBuild(userId: number, dto: CreatePcBuildDto) {
    await this.ensureUser(userId);

    const build = this.pcBuildRepository.create({
      userId,
      name: dto.name,
      description: dto.description ?? null,
      purpose: dto.purpose,
      budget: dto.budget ?? null,
      totalPrice: 0,
      shareCode: null,
      viewCount: 0,
    });

    const saved = await this.pcBuildRepository.save(build);
    return this.getBuildDetail(saved.id);
  }

  async getBuildDetail(buildId: number) {
    const build = await this.findBuildWithParts(buildId);
    await this.pcBuildRepository.increment({ id: buildId }, 'viewCount', 1);

    return this.toBuildDetail(build);
  }

  async updateBuild(userId: number, buildId: number, dto: UpdatePcBuildDto) {
    const build = await this.ensureOwnerBuild(userId, buildId);

    if (dto.name !== undefined) build.name = dto.name;
    if (dto.description !== undefined) build.description = dto.description ?? null;
    if (dto.purpose !== undefined) build.purpose = dto.purpose;
    if (dto.budget !== undefined) build.budget = dto.budget ?? null;

    await this.pcBuildRepository.save(build);
    return this.getBuildDetail(buildId);
  }

  async removeBuild(userId: number, buildId: number) {
    await this.ensureOwnerBuild(userId, buildId);

    await this.pcBuildPartRepository.softDelete({ buildId });
    await this.pcBuildRepository.softDelete({ id: buildId });

    return { success: true, message: '견적이 삭제되었습니다.' };
  }

  // 부품 추가 시 판매처 가격을 선택하고 총액을 갱신한다.
  async addPart(userId: number, buildId: number, dto: AddPcBuildPartDto) {
    await this.ensureOwnerBuild(userId, buildId);

    const product = await this.productRepository.findOne({ where: { id: dto.productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const selectedPrice = await this.findPriceEntry(dto.productId, dto.sellerId);

    let part = await this.pcBuildPartRepository.findOne({
      where: { buildId, partType: dto.partType },
      relations: { product: true, seller: true },
    });

    if (!part) {
      part = this.pcBuildPartRepository.create({
        buildId,
        productId: dto.productId,
        sellerId: selectedPrice.sellerId,
        partType: dto.partType,
        quantity: dto.quantity,
        unitPrice: selectedPrice.price,
        totalPrice: selectedPrice.price * dto.quantity,
      });
    } else {
      part.productId = dto.productId;
      part.sellerId = selectedPrice.sellerId;
      part.quantity = dto.quantity;
      part.unitPrice = selectedPrice.price;
      part.totalPrice = selectedPrice.price * dto.quantity;
    }

    await this.pcBuildPartRepository.save(part);
    await this.syncBuildTotalPrice(buildId);

    return this.getBuildDetail(buildId);
  }

  async removePart(userId: number, buildId: number, partId: number) {
    await this.ensureOwnerBuild(userId, buildId);

    const part = await this.pcBuildPartRepository.findOne({ where: { id: partId, buildId } });
    if (!part) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.pcBuildPartRepository.softDelete({ id: partId });
    await this.syncBuildTotalPrice(buildId);

    return this.getBuildDetail(buildId);
  }

  async getCompatibility(buildId: number) {
    const build = await this.findBuildWithParts(buildId);
    return this.evaluateCompatibility(build.parts ?? []);
  }

  async createShareLink(userId: number, buildId: number) {
    const build = await this.ensureOwnerBuild(userId, buildId);

    if (!build.shareCode) {
      build.shareCode = this.generateShareCode();
      await this.pcBuildRepository.save(build);
    }

    return {
      shareCode: build.shareCode,
      shareUrl: `/pc-builds/shared/${build.shareCode}`,
    };
  }

  async getSharedBuild(shareCode: string) {
    const build = await this.pcBuildRepository.findOne({ where: { shareCode } });
    if (!build) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return this.getBuildDetail(build.id);
  }

  async getPopularBuilds(query: PcBuildListQueryDto) {
    const [items, total] = await this.pcBuildRepository.findAndCount({
      order: { viewCount: 'DESC', updatedAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const summaries = items.map((item) => this.toBuildSummary(item));
    return new PaginationResponseDto(summaries, total, query.page, query.limit);
  }

  async getCompatibilityRules() {
    const items = await this.pcCompatibilityRuleRepository.find({ order: { id: 'ASC' } });
    return items.map((item) => this.toRuleDetail(item));
  }

  async createCompatibilityRule(dto: CreateCompatibilityRuleDto) {
    const metadata = this.toRuleMetadata(dto.metadata);
    const rule = this.pcCompatibilityRuleRepository.create({
      partType: dto.partType,
      targetPartType: dto.targetPartType ?? null,
      title: dto.title,
      description: dto.description,
      severity: dto.severity,
      enabled: dto.enabled ?? true,
      metadata,
    });

    const saved = await this.pcCompatibilityRuleRepository.save(rule);
    return this.toRuleDetail(saved);
  }

  async updateCompatibilityRule(ruleId: number, dto: UpdateCompatibilityRuleDto) {
    const rule = await this.pcCompatibilityRuleRepository.findOne({ where: { id: ruleId } });
    if (!rule) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.partType !== undefined) rule.partType = dto.partType;
    if (dto.targetPartType !== undefined) rule.targetPartType = dto.targetPartType;
    if (dto.title !== undefined) rule.title = dto.title;
    if (dto.description !== undefined) rule.description = dto.description;
    if (dto.severity !== undefined) rule.severity = dto.severity;
    if (dto.enabled !== undefined) rule.enabled = dto.enabled;
    if (dto.metadata !== undefined) rule.metadata = this.toRuleMetadata(dto.metadata);

    const saved = await this.pcCompatibilityRuleRepository.save(rule);
    return this.toRuleDetail(saved);
  }

  async removeCompatibilityRule(ruleId: number) {
    const rule = await this.pcCompatibilityRuleRepository.findOne({ where: { id: ruleId } });
    if (!rule) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.pcCompatibilityRuleRepository.softDelete({ id: ruleId });
    return { success: true, message: '호환성 규칙이 삭제되었습니다.' };
  }

  private async ensureUser(userId: number) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return user;
  }

  private async ensureOwnerBuild(userId: number, buildId: number) {
    const build = await this.pcBuildRepository.findOne({ where: { id: buildId, userId } });
    if (!build) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return build;
  }

  private async findBuildWithParts(buildId: number) {
    const build = await this.pcBuildRepository.findOne({
      where: { id: buildId },
      relations: { parts: { product: true, seller: true } },
    });

    if (!build) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return build;
  }

  private async findPriceEntry(productId: number, sellerId?: number) {
    if (sellerId) {
      const entry = await this.priceEntryRepository.findOne({ where: { productId, sellerId, isAvailable: true } });
      if (!entry) {
        throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
      }
      return entry;
    }

    const entry = await this.priceEntryRepository.findOne({
      where: { productId, isAvailable: true },
      order: { price: 'ASC' },
    });

    if (!entry) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '선택 가능한 판매처 가격이 없습니다.');
    }

    return entry;
  }

  private async syncBuildTotalPrice(buildId: number) {
    const parts = await this.pcBuildPartRepository.find({ where: { buildId } });
    const totalPrice = parts.reduce((sum, part) => sum + part.totalPrice, 0);
    await this.pcBuildRepository.update(buildId, { totalPrice });
  }

  // 선택된 부품 조합의 기본 호환성 상태를 계산한다.
  private async evaluateCompatibility(parts: PcBuildPart[]) {
    if (parts.length === 0) {
      return {
        status: 'EMPTY',
        issues: [],
        warnings: [],
        missingParts: PcBuilderService.REQUIRED_PARTS,
      };
    }

    const selectedTypes = new Set(parts.map((part) => part.partType));
    const missingParts = PcBuilderService.REQUIRED_PARTS.filter((type) => !selectedTypes.has(type));

    const rules = await this.pcCompatibilityRuleRepository.find({ where: { enabled: true } });
    const warnings: Array<{ type: string; message: string; severity: string }> = [];

    for (const rule of rules) {
      if (!selectedTypes.has(rule.partType)) {
        continue;
      }

      if (rule.targetPartType && !selectedTypes.has(rule.targetPartType)) {
        warnings.push({
          type: 'RULE',
          message: rule.description,
          severity: rule.severity,
        });
      }
    }

    const totalWattage = parts.reduce((sum, part) => sum + this.getEstimatedWattage(part.partType) * part.quantity, 0);
    const psuPart = parts.find((part) => part.partType === PcPartType.PSU);
    const psuWattage = psuPart ? 650 : 0;

    return {
      status: missingParts.length > 0 ? 'INCOMPLETE' : warnings.length > 0 ? 'WARNING' : 'OK',
      issues: [],
      warnings,
      missingParts,
      powerEstimate: {
        totalWattage,
        psuWattage,
        headroom: Math.max(0, psuWattage - totalWattage),
        sufficient: psuWattage === 0 ? false : psuWattage >= totalWattage,
      },
      socketCompatible: true,
      ramCompatible: true,
      formFactorCompatible: true,
    };
  }

  private getEstimatedWattage(partType: PcPartType) {
    switch (partType) {
      case PcPartType.CPU:
        return 125;
      case PcPartType.GPU:
        return 250;
      case PcPartType.MOTHERBOARD:
        return 60;
      case PcPartType.RAM:
        return 12;
      case PcPartType.SSD:
      case PcPartType.HDD:
        return 10;
      case PcPartType.COOLER:
        return 8;
      case PcPartType.MONITOR:
        return 35;
      case PcPartType.CASE:
      case PcPartType.PSU:
      default:
        return 5;
    }
  }

  private generateShareCode() {
    return Math.random().toString(36).slice(2, 10).toUpperCase();
  }

  private toRuleMetadata(metadata?: { required?: boolean } | null): Record<string, unknown> | null {
    if (!metadata) {
      return null;
    }

    return {
      required: metadata.required ?? false,
    };
  }

  private toBuildSummary(build: PcBuild) {
    return {
      id: build.id,
      name: build.name,
      purpose: build.purpose,
      budget: build.budget,
      totalPrice: build.totalPrice,
      shareCode: build.shareCode,
      viewCount: build.viewCount,
      updatedAt: build.updatedAt,
    };
  }

  private async toBuildDetail(build: PcBuild) {
    const compatibility = await this.evaluateCompatibility(build.parts ?? []);

    return {
      id: build.id,
      userId: build.userId,
      name: build.name,
      description: build.description,
      purpose: build.purpose,
      budget: build.budget,
      totalPrice: build.totalPrice,
      shareCode: build.shareCode,
      viewCount: build.viewCount,
      parts: (build.parts ?? []).map((part) => ({
        id: part.id,
        partType: part.partType,
        quantity: part.quantity,
        product: part.product
          ? {
              id: part.product.id,
              name: part.product.name,
              lowestPrice: part.product.lowestPrice,
            }
          : null,
        seller: part.seller
          ? {
              id: part.seller.id,
              name: part.seller.name,
              price: part.unitPrice,
            }
          : null,
        unitPrice: part.unitPrice,
        totalPrice: part.totalPrice,
      })),
      compatibility,
      createdAt: build.createdAt,
      updatedAt: build.updatedAt,
    };
  }

  private toRuleDetail(rule: PcCompatibilityRule) {
    return {
      id: rule.id,
      partType: rule.partType,
      targetPartType: rule.targetPartType,
      title: rule.title,
      description: rule.description,
      severity: rule.severity,
      enabled: rule.enabled,
      metadata: rule.metadata,
      createdAt: rule.createdAt,
      updatedAt: rule.updatedAt,
    };
  }
}
