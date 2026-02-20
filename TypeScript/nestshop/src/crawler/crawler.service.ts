import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { Seller } from '../seller/entities/seller.entity';
import { CreateCrawlerJobDto } from './dto/create-crawler-job.dto';
import { CrawlerJobQueryDto } from './dto/crawler-job-query.dto';
import { CrawlerRunQueryDto } from './dto/crawler-run-query.dto';
import { TriggerCrawlerDto } from './dto/trigger-crawler.dto';
import { UpdateCrawlerJobDto } from './dto/update-crawler-job.dto';
import { CrawlerJob } from './entities/crawler-job.entity';
import { CrawlerRun, CrawlerRunStatus, CrawlerTriggerType } from './entities/crawler-run.entity';

@Injectable()
export class CrawlerService {
  constructor(
    @InjectRepository(CrawlerJob)
    private readonly crawlerJobRepository: Repository<CrawlerJob>,
    @InjectRepository(CrawlerRun)
    private readonly crawlerRunRepository: Repository<CrawlerRun>,
    @InjectRepository(Seller)
    private readonly sellerRepository: Repository<Seller>,
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

    const run = await this.executeRun({
      jobId: job.id,
      sellerId: job.sellerId,
      productId: null,
      triggerType: CrawlerTriggerType.MANUAL,
      collectPrice: job.collectPrice,
      collectSpec: job.collectSpec,
      detectAnomaly: job.detectAnomaly,
    });

    job.lastTriggeredAt = run.endedAt;
    await this.crawlerJobRepository.save(job);

    return this.toRunDetail(run);
  }

  // 특정 판매처/상품 대상으로 즉시 수집 실행
  async triggerManual(dto: TriggerCrawlerDto) {
    await this.ensureSeller(dto.sellerId);

    const run = await this.executeRun({
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

    const successRate = total > 0 ? Number(((success / total) * 100).toFixed(2)) : 0;

    return {
      totalRuns: total,
      successRuns: success,
      failedRuns: failed,
      successRate,
      latestRunAt: latest?.startedAt ?? null,
      latestSuccessAt: latestSuccess?.endedAt ?? null,
    };
  }

  private async executeRun(params: {
    jobId: number | null;
    sellerId: number;
    productId: number | null;
    triggerType: CrawlerTriggerType;
    collectPrice: boolean;
    collectSpec: boolean;
    detectAnomaly: boolean;
  }) {
    const startedAt = new Date();

    let status = CrawlerRunStatus.SUCCESS;
    let errorMessage: string | null = null;

    // 실제 크롤러/큐 연동 전 단계에서는 입력 기반으로 수집량을 결정해 실행 로그를 남긴다.
    const priceCount = params.collectPrice ? (params.productId ? 1 : 12) : 0;
    const specCount = params.collectSpec ? (params.productId ? 1 : 8) : 0;
    const anomalyCount = params.detectAnomaly
      ? this.calculateAnomalyCount(priceCount, params.productId)
      : 0;

    try {
      if (!params.collectPrice && !params.collectSpec) {
        throw new Error('가격/스펙 수집 옵션이 모두 비활성화되었습니다.');
      }
    } catch (error) {
      status = CrawlerRunStatus.FAILED;
      errorMessage = error instanceof Error ? error.message : 'Unknown error';
    }

    const endedAt = new Date();

    const run = this.crawlerRunRepository.create({
      jobId: params.jobId,
      sellerId: params.sellerId,
      productId: params.productId,
      triggerType: params.triggerType,
      status,
      startedAt,
      endedAt,
      durationMs: Math.max(0, endedAt.getTime() - startedAt.getTime()),
      collectedPriceCount: priceCount,
      collectedSpecCount: specCount,
      anomalyCount,
      errorMessage,
    });

    return this.crawlerRunRepository.save(run);
  }

  // 급격한 가격 변동 의심치: 수집 건수가 있고 특정 상품 조건을 만족하면 이상치로 표시한다.
  private calculateAnomalyCount(priceCount: number, productId: number | null) {
    if (priceCount === 0) {
      return 0;
    }

    if (!productId) {
      return Math.min(2, Math.floor(priceCount / 6));
    }

    return productId % 3 === 0 ? 1 : 0;
  }

  private async ensureSeller(sellerId: number) {
    const seller = await this.sellerRepository.findOne({ where: { id: sellerId } });
    if (!seller) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '판매처를 찾을 수 없습니다.');
    }
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
