import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { ILike, Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { Product } from '../product/entities/product.entity';
import { ApproveMappingDto } from './dto/approve-mapping.dto';
import { MappingPendingQueryDto } from './dto/mapping-pending-query.dto';
import { RejectMappingDto } from './dto/reject-mapping.dto';
import { ProductMapping, ProductMappingStatus } from './entities/product-mapping.entity';

@Injectable()
export class MatchingService {
  constructor(
    @InjectRepository(ProductMapping)
    private productMappingRepository: Repository<ProductMapping>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
  ) {}

  async getPendingList(query: MappingPendingQueryDto) {
    const [items, total] = await this.productMappingRepository.findAndCount({
      where: { status: ProductMappingStatus.PENDING },
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    return new PaginationResponseDto(items.map((item) => this.toMappingDetail(item)), total, query.page, query.limit);
  }

  async approveMapping(mappingId: number, dto: ApproveMappingDto, adminUserId: number) {
    const mapping = await this.ensurePendingMapping(mappingId);

    const product = await this.productRepository.findOne({ where: { id: dto.productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    mapping.status = ProductMappingStatus.APPROVED;
    mapping.productId = dto.productId;
    mapping.reason = null;
    mapping.reviewedBy = adminUserId;
    mapping.reviewedAt = new Date();

    const saved = await this.productMappingRepository.save(mapping);
    return this.toMappingDetail(saved);
  }

  async rejectMapping(mappingId: number, dto: RejectMappingDto, adminUserId: number) {
    const mapping = await this.ensurePendingMapping(mappingId);

    mapping.status = ProductMappingStatus.REJECTED;
    mapping.reason = dto.reason;
    mapping.reviewedBy = adminUserId;
    mapping.reviewedAt = new Date();

    const saved = await this.productMappingRepository.save(mapping);
    return this.toMappingDetail(saved);
  }

  // sourceName 기반 유사 이름 매칭으로 대기 건을 자동 승인한다.
  async autoMatch(adminUserId: number) {
    const pendings = await this.productMappingRepository.find({
      where: { status: ProductMappingStatus.PENDING },
      order: { createdAt: 'ASC' },
      take: 200,
    });

    let matchedCount = 0;

    for (const mapping of pendings) {
      const product = await this.findCandidateProduct(mapping.sourceName);
      if (!product) {
        continue;
      }

      mapping.status = ProductMappingStatus.APPROVED;
      mapping.productId = product.id;
      mapping.confidence = '75.00';
      mapping.reason = null;
      mapping.reviewedBy = adminUserId;
      mapping.reviewedAt = new Date();

      await this.productMappingRepository.save(mapping);
      matchedCount += 1;
    }

    const pendingCount = await this.productMappingRepository.count({
      where: { status: ProductMappingStatus.PENDING },
    });

    return { matchedCount, pendingCount };
  }

  async getStats() {
    const [pending, approved, rejected] = await Promise.all([
      this.productMappingRepository.count({ where: { status: ProductMappingStatus.PENDING } }),
      this.productMappingRepository.count({ where: { status: ProductMappingStatus.APPROVED } }),
      this.productMappingRepository.count({ where: { status: ProductMappingStatus.REJECTED } }),
    ]);

    return {
      pending,
      approved,
      rejected,
      total: pending + approved + rejected,
    };
  }

  private async ensurePendingMapping(mappingId: number) {
    const mapping = await this.productMappingRepository.findOne({ where: { id: mappingId } });
    if (!mapping) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (mapping.status !== ProductMappingStatus.PENDING) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '대기 상태 매핑만 처리할 수 있습니다.');
    }

    return mapping;
  }

  private async findCandidateProduct(sourceName: string) {
    const trimmed = sourceName.trim();
    if (!trimmed) {
      return null;
    }

    const firstWord = trimmed.split(' ')[0];

    return this.productRepository.findOne({
      where: [{ name: ILike(`%${trimmed}%`) }, { name: ILike(`%${firstWord}%`) }],
      order: { salesCount: 'DESC', popularityScore: 'DESC' },
    });
  }

  private toMappingDetail(item: ProductMapping) {
    return {
      id: item.id,
      sourceName: item.sourceName,
      sourceBrand: item.sourceBrand,
      sourceSeller: item.sourceSeller,
      sourceUrl: item.sourceUrl,
      status: item.status,
      productId: item.productId,
      confidence: Number(item.confidence),
      reason: item.reason,
      reviewedBy: item.reviewedBy,
      reviewedAt: item.reviewedAt,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    };
  }
}
