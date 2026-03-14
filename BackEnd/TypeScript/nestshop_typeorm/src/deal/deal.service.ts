import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, MoreThan, Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { Product } from '../product/entities/product.entity';
import { CreateDealDto } from './dto/create-deal.dto';
import { DealQueryDto } from './dto/deal-query.dto';
import { UpdateDealDto } from './dto/update-deal.dto';
import { Deal } from './entities/deal.entity';

@Injectable()
export class DealService {
  constructor(
    @InjectRepository(Deal)
    private dealRepository: Repository<Deal>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
  ) {}

  // DEAL-01: 특가 목록 조회
  async findDeals(query: DealQueryDto) {
    const now = new Date();

    const where = query.activeOnly
      ? { isActive: true, endAt: MoreThan(now) }
      : {};

    const items = await this.dealRepository.find({
      where,
      order: { startAt: 'DESC' },
      take: query.limit,
    });

    const productIds = [...new Set(items.map((item) => item.productId))];
    const products = productIds.length
      ? await this.productRepository.findBy({ id: In(productIds) })
      : [];
    const productMap = new Map(products.map((p) => [p.id, p]));

    return items.map((deal) => this.toDetail(deal, productMap.get(deal.productId) ?? null));
  }

  // DEAL-02: 특가 등록 (Admin)
  async create(dto: CreateDealDto) {
    await this.ensureProduct(dto.productId);
    this.assertDealTime(dto.startAt, dto.endAt);

    const deal = this.dealRepository.create({
      productId: dto.productId,
      title: dto.title,
      description: dto.description ?? null,
      discountRate: dto.discountRate,
      startAt: dto.startAt,
      endAt: dto.endAt,
      isActive: dto.isActive ?? true,
    });

    const saved = await this.dealRepository.save(deal);
    const product = await this.productRepository.findOne({ where: { id: saved.productId } });
    return this.toDetail(saved, product);
  }

  // DEAL-03: 특가 수정 (Admin)
  async update(id: number, dto: UpdateDealDto) {
    const deal = await this.dealRepository.findOne({ where: { id } });
    if (!deal) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.productId !== undefined) {
      await this.ensureProduct(dto.productId);
      deal.productId = dto.productId;
    }
    if (dto.title !== undefined) deal.title = dto.title;
    if (dto.description !== undefined) deal.description = dto.description;
    if (dto.discountRate !== undefined) deal.discountRate = dto.discountRate;
    if (dto.startAt !== undefined) deal.startAt = dto.startAt;
    if (dto.endAt !== undefined) deal.endAt = dto.endAt;
    if (dto.isActive !== undefined) deal.isActive = dto.isActive;

    this.assertDealTime(deal.startAt, deal.endAt);

    const saved = await this.dealRepository.save(deal);
    const product = await this.productRepository.findOne({ where: { id: saved.productId } });
    return this.toDetail(saved, product);
  }

  // DEAL-04: 특가 삭제 (Admin)
  async remove(id: number) {
    const deal = await this.dealRepository.findOne({ where: { id } });
    if (!deal) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.dealRepository.softRemove(deal);
    return { message: '특가가 삭제되었습니다.' };
  }

  private async ensureProduct(productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
  }

  private assertDealTime(startAt: Date, endAt: Date) {
    if (startAt >= endAt) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '딜 시작 시각은 종료 시각보다 빨라야 합니다.');
    }
  }

  private toDetail(deal: Deal, product: Product | null) {
    return {
      id: deal.id,
      productId: deal.productId,
      product: product
        ? {
            id: product.id,
            name: product.name,
            thumbnailUrl: product.thumbnailUrl,
            lowestPrice: product.lowestPrice,
          }
        : null,
      title: deal.title,
      description: deal.description,
      discountRate: deal.discountRate,
      startAt: deal.startAt,
      endAt: deal.endAt,
      isActive: deal.isActive,
      createdAt: deal.createdAt,
      updatedAt: deal.updatedAt,
    };
  }
}

