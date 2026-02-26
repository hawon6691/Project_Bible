import { InjectQueue } from '@nestjs/bull';
import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Queue } from 'bull';
import { Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { SearchService } from '../search/search.service';
import {
  SearchIndexOutbox,
  SearchIndexOutboxEventType,
  SearchIndexOutboxStatus,
} from './entities/search-index-outbox.entity';

export interface SearchSyncJobData {
  outboxId: number;
}

@Injectable()
export class SearchSyncService {
  constructor(
    @InjectRepository(SearchIndexOutbox)
    private readonly outboxRepository: Repository<SearchIndexOutbox>,
    private readonly searchService: SearchService,
    @InjectQueue('search-index-sync')
    private readonly searchIndexSyncQueue: Queue<SearchSyncJobData>,
  ) {}

  async enqueueProductUpsert(productId: number, reason?: string) {
    return this.enqueue(SearchIndexOutboxEventType.PRODUCT_UPSERT, productId, { reason: reason ?? null });
  }

  async enqueueProductDelete(productId: number, reason?: string) {
    return this.enqueue(SearchIndexOutboxEventType.PRODUCT_DELETE, productId, { reason: reason ?? null });
  }

  async enqueuePriceChanged(productId: number, priceEntryId?: number) {
    return this.enqueue(SearchIndexOutboxEventType.PRICE_CHANGED, productId, {
      priceEntryId: priceEntryId ?? null,
    });
  }

  async processOutbox(outboxId: number) {
    const outbox = await this.outboxRepository.findOne({ where: { id: outboxId } });
    if (!outbox) return;

    await this.outboxRepository.update(outbox.id, {
      status: SearchIndexOutboxStatus.PROCESSING,
      attemptCount: outbox.attemptCount + 1,
      lastError: null,
    });

    try {
      if (outbox.eventType === SearchIndexOutboxEventType.PRODUCT_DELETE) {
        await this.searchService.removeProductDocument(outbox.aggregateId);
      } else {
        await this.searchService.reindexProduct(outbox.aggregateId);
      }

      await this.outboxRepository.update(outbox.id, {
        status: SearchIndexOutboxStatus.COMPLETED,
        processedAt: new Date(),
        lastError: null,
      });
    } catch (error) {
      await this.outboxRepository.update(outbox.id, {
        status: SearchIndexOutboxStatus.FAILED,
        lastError: error instanceof Error ? error.message.slice(0, 500) : 'Unknown error',
      });
      throw error;
    }
  }

  async getOutboxSummary() {
    const [pending, processing, completed, failed] = await Promise.all([
      this.outboxRepository.count({ where: { status: SearchIndexOutboxStatus.PENDING } }),
      this.outboxRepository.count({ where: { status: SearchIndexOutboxStatus.PROCESSING } }),
      this.outboxRepository.count({ where: { status: SearchIndexOutboxStatus.COMPLETED } }),
      this.outboxRepository.count({ where: { status: SearchIndexOutboxStatus.FAILED } }),
    ]);

    return {
      pending,
      processing,
      completed,
      failed,
    };
  }

  async requeueFailed(limit = 100) {
    const failedRows = await this.outboxRepository.find({
      where: { status: SearchIndexOutboxStatus.FAILED },
      order: { updatedAt: 'DESC' },
      take: limit,
    });

    for (const row of failedRows) {
      await this.outboxRepository.update(row.id, {
        status: SearchIndexOutboxStatus.PENDING,
        lastError: null,
      });
      await this.searchIndexSyncQueue.add(
        'sync',
        { outboxId: row.id },
        {
          attempts: 3,
          backoff: { type: 'fixed', delay: 1000 },
          removeOnComplete: true,
        },
      );
    }

    return { requeuedCount: failedRows.length };
  }

  private async enqueue(
    eventType: SearchIndexOutboxEventType,
    aggregateId: number,
    payload: Record<string, unknown>,
  ) {
    if (!Number.isInteger(aggregateId) || aggregateId <= 0) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '유효한 aggregateId가 필요합니다.');
    }

    const outbox = this.outboxRepository.create({
      eventType,
      aggregateId,
      payload,
      status: SearchIndexOutboxStatus.PENDING,
      attemptCount: 0,
      lastError: null,
      processedAt: null,
    });

    const saved = await this.outboxRepository.save(outbox);

    await this.searchIndexSyncQueue.add(
      'sync',
      { outboxId: saved.id },
      {
        attempts: 3,
        backoff: { type: 'fixed', delay: 1000 },
        removeOnComplete: true,
      },
    );

    return saved.id;
  }
}
