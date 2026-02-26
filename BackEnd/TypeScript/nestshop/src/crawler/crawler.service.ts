import { InjectQueue } from '@nestjs/bull';
import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Queue } from 'bull';
import { In, Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PriceEntry, ShippingType } from '../price/entities/price-entry.entity';
import { Product, ProductStatus } from '../product/entities/product.entity';
import { Seller } from '../seller/entities/seller.entity';
import { ProductSpec } from '../spec/entities/product-spec.entity';
import { SpecDataType, SpecDefinition } from '../spec/entities/spec-definition.entity';
import { CreateCrawlerJobDto } from './dto/create-crawler-job.dto';
import { CrawlerJobQueryDto } from './dto/crawler-job-query.dto';
import { CrawlerRunQueryDto } from './dto/crawler-run-query.dto';
import { TriggerCrawlerDto } from './dto/trigger-crawler.dto';
import { UpdateCrawlerJobDto } from './dto/update-crawler-job.dto';
import { CrawlerJob } from './entities/crawler-job.entity';
import { CrawlerRun, CrawlerRunStatus, CrawlerTriggerType } from './entities/crawler-run.entity';

export interface CrawlerCollectJobData {
  runId: number;
}

@Injectable()
export class CrawlerService {
  constructor(
    @InjectRepository(CrawlerJob)
    private readonly crawlerJobRepository: Repository<CrawlerJob>,
    @InjectRepository(CrawlerRun)
    private readonly crawlerRunRepository: Repository<CrawlerRun>,
    @InjectRepository(Seller)
    private readonly sellerRepository: Repository<Seller>,
    @InjectRepository(Product)
    private readonly productRepository: Repository<Product>,
    @InjectRepository(PriceEntry)
    private readonly priceEntryRepository: Repository<PriceEntry>,
    @InjectRepository(SpecDefinition)
    private readonly specDefinitionRepository: Repository<SpecDefinition>,
    @InjectRepository(ProductSpec)
    private readonly productSpecRepository: Repository<ProductSpec>,
    @InjectQueue('crawler-collect')
    private readonly crawlerCollectQueue: Queue<CrawlerCollectJobData>,
  ) {}

  async createJob(dto: CreateCrawlerJobDto) {
    await this.ensureSeller(dto.sellerId);

    const job = this.crawlerJobRepository.create({
      sellerId: dto.sellerId,
      name: dto.name,
      cronExpression: dto.cronExpression ?? null,
      collectPrice: dto.collectPrice ?? true,
      collectSpec: dto.collectSpec ?? true,
      detectAnomaly: dto.detectAnomaly ?? true,
      isActive: dto.isActive ?? true,
      lastTriggeredAt: null,
    });

    const saved = await this.crawlerJobRepository.save(job);
    return this.toJobDetail(saved);
  }

  async getJobs(query: CrawlerJobQueryDto) {
    const qb = this.crawlerJobRepository
      .createQueryBuilder('job')
      .orderBy('job.createdAt', 'DESC')
      .skip(query.skip)
      .take(query.limit);

    if (query.sellerId !== undefined) {
      qb.andWhere('job.sellerId = :sellerId', { sellerId: query.sellerId });
    }

    if (query.isActive !== undefined) {
      qb.andWhere('job.isActive = :isActive', { isActive: query.isActive });
    }

    const [items, total] = await qb.getManyAndCount();
    return new PaginationResponseDto(items.map((item) => this.toJobDetail(item)), total, query.page, query.limit);
  }

  async updateJob(jobId: number, dto: UpdateCrawlerJobDto) {
    const job = await this.crawlerJobRepository.findOne({ where: { id: jobId } });
    if (!job) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '크롤러 작업을 찾을 수 없습니다.');
    }

    if (dto.sellerId !== undefined) {
      await this.ensureSeller(dto.sellerId);
      job.sellerId = dto.sellerId;
    }

    if (dto.name !== undefined) job.name = dto.name;
    if (dto.cronExpression !== undefined) job.cronExpression = dto.cronExpression;
    if (dto.collectPrice !== undefined) job.collectPrice = dto.collectPrice;
    if (dto.collectSpec !== undefined) job.collectSpec = dto.collectSpec;
    if (dto.detectAnomaly !== undefined) job.detectAnomaly = dto.detectAnomaly;
    if (dto.isActive !== undefined) job.isActive = dto.isActive;

    const saved = await this.crawlerJobRepository.save(job);
    return this.toJobDetail(saved);
  }

  async removeJob(jobId: number) {
    const job = await this.crawlerJobRepository.findOne({ where: { id: jobId } });
    if (!job) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '크롤러 작업을 찾을 수 없습니다.');
    }

    await this.crawlerJobRepository.softDelete({ id: jobId });
    return { success: true, message: '크롤러 작업이 삭제되었습니다.' };
  }

  async triggerJob(jobId: number) {
    const job = await this.crawlerJobRepository.findOne({ where: { id: jobId } });
    if (!job) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '크롤러 작업을 찾을 수 없습니다.');
    }

    if (!job.isActive) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '비활성화된 작업은 실행할 수 없습니다.');
    }

    const run = await this.createQueuedRun({
      jobId: job.id,
      sellerId: job.sellerId,
      productId: null,
      triggerType: CrawlerTriggerType.MANUAL,
      collectPrice: job.collectPrice,
      collectSpec: job.collectSpec,
      detectAnomaly: job.detectAnomaly,
    });

    job.lastTriggeredAt = new Date();
    await this.crawlerJobRepository.save(job);
    return this.toRunDetail(run);
  }

  // 특정 판매처/상품 대상으로 즉시 수집 실행을 큐에 등록한다.
  async triggerManual(dto: TriggerCrawlerDto) {
    await this.ensureSeller(dto.sellerId);

    const run = await this.createQueuedRun({
      jobId: null,
      sellerId: dto.sellerId,
      productId: dto.productId ?? null,
      triggerType: CrawlerTriggerType.MANUAL,
      collectPrice: dto.collectPrice ?? true,
      collectSpec: dto.collectSpec ?? true,
      detectAnomaly: dto.detectAnomaly ?? true,
    });

    return this.toRunDetail(run);
  }

  async getRuns(query: CrawlerRunQueryDto) {
    const qb = this.crawlerRunRepository
      .createQueryBuilder('run')
      .orderBy('run.startedAt', 'DESC')
      .skip(query.skip)
      .take(query.limit);

    if (query.jobId !== undefined) {
      qb.andWhere('run.jobId = :jobId', { jobId: query.jobId });
    }

    if (query.sellerId !== undefined) {
      qb.andWhere('run.sellerId = :sellerId', { sellerId: query.sellerId });
    }

    if (query.status !== undefined) {
      qb.andWhere('run.status = :status', { status: query.status });
    }

    const [items, total] = await qb.getManyAndCount();
    return new PaginationResponseDto(items.map((item) => this.toRunDetail(item)), total, query.page, query.limit);
  }

  async getMonitoringSummary() {
    const total = await this.crawlerRunRepository.count();
    const queued = await this.crawlerRunRepository.count({ where: { status: CrawlerRunStatus.QUEUED } });
    const processing = await this.crawlerRunRepository.count({ where: { status: CrawlerRunStatus.PROCESSING } });
    const success = await this.crawlerRunRepository.count({ where: { status: CrawlerRunStatus.SUCCESS } });
    const failed = await this.crawlerRunRepository.count({ where: { status: CrawlerRunStatus.FAILED } });

    const latest = await this.crawlerRunRepository.findOne({
      where: {},
      order: { startedAt: 'DESC' },
    });

    const latestSuccess = await this.crawlerRunRepository.findOne({
      where: { status: CrawlerRunStatus.SUCCESS },
      order: { endedAt: 'DESC' },
    });

    const doneCount = success + failed;
    const successRate = doneCount > 0 ? Number(((success / doneCount) * 100).toFixed(2)) : 0;

    return {
      totalRuns: total,
      queuedRuns: queued,
      processingRuns: processing,
      successRuns: success,
      failedRuns: failed,
      successRate,
      latestRunAt: latest?.startedAt ?? null,
      latestSuccessAt: latestSuccess?.endedAt ?? null,
    };
  }

  // 워커 프로세서에서 실제 수집/업서트를 수행한다.
  async processRun(runId: number) {
    const run = await this.crawlerRunRepository.findOne({ where: { id: runId } });
    if (!run) {
      return;
    }

    const startedAt = new Date();
    await this.crawlerRunRepository.update(run.id, {
      status: CrawlerRunStatus.PROCESSING,
      startedAt,
      errorMessage: null,
    });

    try {
      const seller = await this.ensureSeller(run.sellerId);
      const products = await this.resolveTargetProducts(run.productId);

      if (!products.length) {
        throw new Error('수집 대상 상품이 없습니다.');
      }

      let collectedPriceCount = 0;
      let collectedSpecCount = 0;
      let anomalyCount = 0;

      if (run.collectPrice) {
        const priceResult = await this.collectPrices(seller, products, run.detectAnomaly);
        collectedPriceCount += priceResult.collectedCount;
        anomalyCount += priceResult.anomalyCount;
      }

      if (run.collectSpec) {
        const specResult = await this.collectSpecs(products);
        collectedSpecCount += specResult.collectedCount;
      }

      const endedAt = new Date();
      await this.crawlerRunRepository.update(run.id, {
        status: CrawlerRunStatus.SUCCESS,
        endedAt,
        durationMs: Math.max(0, endedAt.getTime() - startedAt.getTime()),
        collectedPriceCount,
        collectedSpecCount,
        anomalyCount,
        errorMessage: null,
      });
    } catch (error) {
      const endedAt = new Date();
      await this.crawlerRunRepository.update(run.id, {
        status: CrawlerRunStatus.FAILED,
        endedAt,
        durationMs: Math.max(0, endedAt.getTime() - startedAt.getTime()),
        errorMessage: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  }

  private async createQueuedRun(params: {
    jobId: number | null;
    sellerId: number;
    productId: number | null;
    triggerType: CrawlerTriggerType;
    collectPrice: boolean;
    collectSpec: boolean;
    detectAnomaly: boolean;
  }) {
    if (!params.collectPrice && !params.collectSpec) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '가격/스펙 수집 옵션이 모두 비활성화되었습니다.');
    }

    const now = new Date();
    const run = this.crawlerRunRepository.create({
      jobId: params.jobId,
      sellerId: params.sellerId,
      productId: params.productId,
      triggerType: params.triggerType,
      collectPrice: params.collectPrice,
      collectSpec: params.collectSpec,
      detectAnomaly: params.detectAnomaly,
      status: CrawlerRunStatus.QUEUED,
      startedAt: now,
      endedAt: now,
      durationMs: 0,
      collectedPriceCount: 0,
      collectedSpecCount: 0,
      anomalyCount: 0,
      errorMessage: null,
    });

    const saved = await this.crawlerRunRepository.save(run);
    await this.crawlerCollectQueue.add(
      'collect',
      { runId: saved.id },
      {
        attempts: 2,
        backoff: { type: 'fixed', delay: 1000 },
        removeOnComplete: true,
      },
    );

    return saved;
  }

  private async resolveTargetProducts(productId: number | null) {
    if (productId) {
      const product = await this.productRepository.findOne({ where: { id: productId } });
      if (!product) {
        throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND, '수집 대상 상품을 찾을 수 없습니다.');
      }
      return [product];
    }

    return this.productRepository.find({
      where: { status: ProductStatus.ON_SALE },
      order: { updatedAt: 'DESC' },
      take: 30,
    });
  }

  private async collectPrices(seller: Seller, products: Product[], detectAnomaly: boolean) {
    let collectedCount = 0;
    let anomalyCount = 0;
    const now = new Date();

    const productIds = products.map((product) => product.id);
    const existingEntries = await this.priceEntryRepository.find({
      where: {
        sellerId: seller.id,
        productId: In(productIds),
      },
    });
    const existingByProductId = new Map(existingEntries.map((item) => [item.productId, item]));

    for (const product of products) {
      const existing = existingByProductId.get(product.id);
      const crawledPrice = this.generateCrawledPrice(product.price, seller.id, product.id);
      const shippingCost = this.generateShippingCost(seller.id, product.id);
      const shippingType = shippingCost === 0 ? ShippingType.FREE : ShippingType.PAID;

      if (detectAnomaly) {
        anomalyCount += this.calculateAnomalyCount({
          beforePrice: existing?.price ?? null,
          crawledPrice,
          productPrice: product.price,
        });
      }

      if (existing) {
        existing.price = crawledPrice;
        existing.shippingCost = shippingCost;
        existing.shippingFee = shippingCost;
        existing.shippingType = shippingType;
        existing.shippingInfo = shippingCost === 0 ? '무료배송' : `${shippingCost.toLocaleString()}원`;
        existing.productUrl = this.buildProductUrl(seller.url, product.id);
        existing.isAvailable = true;
        existing.crawledAt = now;
        await this.priceEntryRepository.save(existing);
      } else {
        const entry = this.priceEntryRepository.create({
          productId: product.id,
          sellerId: seller.id,
          price: crawledPrice,
          shippingCost,
          shippingInfo: shippingCost === 0 ? '무료배송' : `${shippingCost.toLocaleString()}원`,
          productUrl: this.buildProductUrl(seller.url, product.id),
          shippingFee: shippingCost,
          shippingType,
          clickCount: 0,
          isAvailable: true,
          crawledAt: now,
        });
        await this.priceEntryRepository.save(entry);
      }

      collectedCount += 1;
    }

    return { collectedCount, anomalyCount };
  }

  private async collectSpecs(products: Product[]) {
    let collectedCount = 0;
    const categoryIds = [...new Set(products.map((item) => item.categoryId))];
    const definitions = await this.specDefinitionRepository.find({
      where: { categoryId: In(categoryIds) },
      order: { sortOrder: 'ASC', id: 'ASC' },
    });

    const defsByCategory = new Map<number, SpecDefinition[]>();
    for (const definition of definitions) {
      const current = defsByCategory.get(definition.categoryId) ?? [];
      current.push(definition);
      defsByCategory.set(definition.categoryId, current);
    }

    for (const product of products) {
      const productDefs = defsByCategory.get(product.categoryId) ?? [];
      for (const definition of productDefs) {
        const generated = this.generateSpecValue(definition, product.id);

        await this.productSpecRepository.upsert(
          {
            productId: product.id,
            specDefinitionId: definition.id,
            value: generated.value,
            numericValue: generated.numericValue,
          },
          ['productId', 'specDefinitionId'],
        );

        collectedCount += 1;
      }
    }

    return { collectedCount };
  }

  private generateCrawledPrice(basePrice: number, sellerId: number, productId: number) {
    const seed = (sellerId * 31 + productId * 17) % 23;
    const ratio = 1 + (seed - 11) / 100;
    const raw = Math.max(100, Math.round(basePrice * ratio));
    return Math.floor(raw / 10) * 10;
  }

  private generateShippingCost(sellerId: number, productId: number) {
    const seed = (sellerId + productId) % 4;
    if (seed === 0) return 0;
    if (seed === 1) return 2500;
    if (seed === 2) return 3000;
    return 3500;
  }

  private buildProductUrl(baseUrl: string, productId: number) {
    const normalized = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    return `${normalized}/products/${productId}`;
  }

  private calculateAnomalyCount(params: {
    beforePrice: number | null;
    crawledPrice: number;
    productPrice: number;
  }) {
    const target = params.beforePrice ?? params.productPrice;
    if (target <= 0) return 0;

    const diffRatio = Math.abs(params.crawledPrice - target) / target;
    return diffRatio >= 0.2 ? 1 : 0;
  }

  private generateSpecValue(definition: SpecDefinition, productId: number) {
    if (definition.options?.length) {
      const option = definition.options[(productId + definition.id) % definition.options.length];
      return { value: option, numericValue: null };
    }

    if (definition.dataType === SpecDataType.BOOLEAN) {
      const boolValue = (productId + definition.id) % 2 === 0;
      return { value: boolValue ? 'true' : 'false', numericValue: null };
    }

    if (definition.dataType === SpecDataType.NUMBER) {
      const numeric = ((productId % 10) + 1) * ((definition.id % 4) + 1);
      const unitSuffix = definition.unit ? ` ${definition.unit}` : '';
      return { value: `${numeric}${unitSuffix}`, numericValue: numeric };
    }

    return { value: `${definition.name}-${productId}`, numericValue: null };
  }

  private async ensureSeller(sellerId: number) {
    const seller = await this.sellerRepository.findOne({ where: { id: sellerId } });
    if (!seller) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '판매처를 찾을 수 없습니다.');
    }
    return seller;
  }

  private toJobDetail(item: CrawlerJob) {
    return {
      id: item.id,
      sellerId: item.sellerId,
      name: item.name,
      cronExpression: item.cronExpression,
      collectPrice: item.collectPrice,
      collectSpec: item.collectSpec,
      detectAnomaly: item.detectAnomaly,
      isActive: item.isActive,
      lastTriggeredAt: item.lastTriggeredAt,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    };
  }

  private toRunDetail(item: CrawlerRun) {
    return {
      id: item.id,
      jobId: item.jobId,
      sellerId: item.sellerId,
      productId: item.productId,
      triggerType: item.triggerType,
      collectPrice: item.collectPrice,
      collectSpec: item.collectSpec,
      detectAnomaly: item.detectAnomaly,
      status: item.status,
      startedAt: item.startedAt,
      endedAt: item.endedAt,
      durationMs: item.durationMs,
      collectedPriceCount: item.collectedPriceCount,
      collectedSpecCount: item.collectedSpecCount,
      anomalyCount: item.anomalyCount,
      errorMessage: item.errorMessage,
      createdAt: item.createdAt,
    };
  }
}
