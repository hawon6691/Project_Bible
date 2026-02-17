import { ForbiddenException, HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PcBuildPart } from '../pc-builder/entities/pc-build-part.entity';
import { PcBuild } from '../pc-builder/entities/pc-build.entity';
import { PriceHistory } from '../price/entities/price-history.entity';
import { Product } from '../product/entities/product.entity';
import { UsedMarketCategoryQueryDto } from './dto/used-market-category-query.dto';

@Injectable()
export class UsedMarketService {
  constructor(
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(PriceHistory)
    private priceHistoryRepository: Repository<PriceHistory>,
    @InjectRepository(PcBuild)
    private pcBuildRepository: Repository<PcBuild>,
    @InjectRepository(PcBuildPart)
    private pcBuildPartRepository: Repository<PcBuildPart>,
  ) {}

  async getProductUsedPrice(productId: number) {
    const product = await this.ensureProduct(productId);
    const basePrice = this.getBasePrice(product);
    const trend = await this.estimateTrend(productId);

    return {
      productId,
      averagePrice: Math.round(basePrice * 0.7),
      minPrice: Math.round(basePrice * 0.55),
      maxPrice: Math.round(basePrice * 0.85),
      trend,
    };
  }

  async getCategoryUsedPrices(categoryId: number, query: UsedMarketCategoryQueryDto) {
    const [products, total] = await this.productRepository.findAndCount({
      where: { categoryId },
      order: { popularityScore: 'DESC', salesCount: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const items = await Promise.all(
      products.map(async (product) => {
        const basePrice = this.getBasePrice(product);
        const trend = await this.estimateTrend(product.id);

        return {
          productId: product.id,
          productName: product.name,
          averagePrice: Math.round(basePrice * 0.7),
          minPrice: Math.round(basePrice * 0.55),
          maxPrice: Math.round(basePrice * 0.85),
          trend,
        };
      }),
    );

    return new PaginationResponseDto(items, total, query.page, query.limit);
  }

  // PC 빌드의 각 부품별 감가를 적용해 중고 매입가를 계산한다.
  async estimatePcBuildUsedPrice(userId: number, buildId: number) {
    const build = await this.pcBuildRepository.findOne({ where: { id: buildId } });
    if (!build) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (build.userId !== userId) {
      throw new ForbiddenException('본인 견적만 조회할 수 있습니다.');
    }

    const parts = await this.pcBuildPartRepository.find({
      where: { buildId },
      relations: { product: true },
      order: { createdAt: 'ASC' },
    });

    const partBreakdown = parts.map((part) => {
      const depreciationRate = this.getDepreciationRate(part.partType);
      const estimatedPrice = Math.round(part.totalPrice * depreciationRate);

      return {
        partId: part.id,
        partType: part.partType,
        productId: part.productId,
        productName: part.product?.name ?? null,
        originalPrice: part.totalPrice,
        depreciationRate,
        estimatedUsedPrice: estimatedPrice,
      };
    });

    const estimatedPrice = partBreakdown.reduce((sum, part) => sum + part.estimatedUsedPrice, 0);

    return {
      buildId,
      estimatedPrice,
      partBreakdown,
    };
  }

  private async ensureProduct(productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return product;
  }

  private getBasePrice(product: Product) {
    return product.lowestPrice ?? product.discountPrice ?? product.price;
  }

  private async estimateTrend(productId: number) {
    const history = await this.priceHistoryRepository.find({
      where: { productId },
      order: { date: 'DESC' },
      take: 2,
    });

    if (history.length < 2) {
      return 'STABLE';
    }

    const latest = history[0].lowestPrice;
    const prev = history[1].lowestPrice;

    if (latest > prev) return 'UP';
    if (latest < prev) return 'DOWN';
    return 'STABLE';
  }

  private getDepreciationRate(partType: string) {
    switch (partType) {
      case 'GPU':
        return 0.6;
      case 'CPU':
      case 'MOTHERBOARD':
      case 'MONITOR':
        return 0.65;
      case 'RAM':
      case 'SSD':
      case 'HDD':
        return 0.7;
      case 'PSU':
      case 'CASE':
      case 'COOLER':
      default:
        return 0.5;
    }
  }
}
