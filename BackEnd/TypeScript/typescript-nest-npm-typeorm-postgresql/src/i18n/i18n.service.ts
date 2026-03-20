import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { ConvertAmountQueryDto } from './dto/convert-amount-query.dto';
import { TranslationQueryDto } from './dto/translation-query.dto';
import { UpsertExchangeRateDto } from './dto/upsert-exchange-rate.dto';
import { UpsertTranslationDto } from './dto/upsert-translation.dto';
import { ExchangeRate } from './entities/exchange-rate.entity';
import { Translation } from './entities/translation.entity';

@Injectable()
export class I18nService {
  constructor(
    @InjectRepository(Translation)
    private translationRepository: Repository<Translation>,
    @InjectRepository(ExchangeRate)
    private exchangeRateRepository: Repository<ExchangeRate>,
  ) {}

  // locale/namespace/key 조건으로 번역을 조회한다.
  async getTranslations(query: TranslationQueryDto) {
    const qb = this.translationRepository
      .createQueryBuilder('translation')
      .orderBy('translation.id', 'ASC');

    if (query.locale) {
      qb.andWhere('translation.locale = :locale', { locale: query.locale });
    }

    if (query.namespace) {
      qb.andWhere('translation.namespace = :namespace', { namespace: query.namespace });
    }

    if (query.key) {
      qb.andWhere('translation.key = :key', { key: query.key });
    }

    const items = await qb.getMany();
    return items.map((item) => this.toTranslationDetail(item));
  }

  // locale + namespace + key 기준으로 번역을 upsert 한다.
  async upsertTranslation(dto: UpsertTranslationDto) {
    let item = await this.translationRepository.findOne({
      where: {
        locale: dto.locale,
        namespace: dto.namespace,
        key: dto.key,
      },
    });

    if (!item) {
      item = this.translationRepository.create({
        locale: dto.locale,
        namespace: dto.namespace,
        key: dto.key,
        value: dto.value,
      });
    } else {
      item.value = dto.value;
    }

    const saved = await this.translationRepository.save(item);
    return this.toTranslationDetail(saved);
  }

  async deleteTranslation(id: number) {
    const result = await this.translationRepository.softDelete({ id });
    if (!result.affected) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return { success: true, message: '번역이 삭제되었습니다.' };
  }

  async getExchangeRates() {
    const items = await this.exchangeRateRepository.find({
      order: { updatedAt: 'DESC' },
    });

    return items.map((item) => this.toExchangeRateDetail(item));
  }

  // 통화쌍 기준 환율을 upsert 한다.
  async upsertExchangeRate(dto: UpsertExchangeRateDto) {
    const baseCurrency = dto.baseCurrency.toUpperCase();
    const targetCurrency = dto.targetCurrency.toUpperCase();

    let item = await this.exchangeRateRepository.findOne({
      where: { baseCurrency, targetCurrency },
    });

    if (!item) {
      item = this.exchangeRateRepository.create({
        baseCurrency,
        targetCurrency,
        rate: dto.rate.toFixed(8),
      });
    } else {
      item.rate = dto.rate.toFixed(8);
    }

    const saved = await this.exchangeRateRepository.save(item);
    return this.toExchangeRateDetail(saved);
  }

  // direct/inverse/KRW 경유 환율을 이용해 금액을 환산한다.
  async convertAmount(query: ConvertAmountQueryDto) {
    const from = query.from.toUpperCase();
    const to = query.to.toUpperCase();

    const rate = await this.resolveRate(from, to);
    const convertedAmount = Number((query.amount * rate).toFixed(2));

    return {
      originalAmount: query.amount,
      originalCurrency: from,
      convertedAmount,
      targetCurrency: to,
      rate,
    };
  }

  private async resolveRate(from: string, to: string) {
    if (from === to) {
      return 1;
    }

    const direct = await this.getDirectOrInverseRate(from, to);
    if (direct !== null) {
      return direct;
    }

    const fromToKrw = await this.getDirectOrInverseRate(from, 'KRW');
    const krwToTo = await this.getDirectOrInverseRate('KRW', to);

    if (fromToKrw !== null && krwToTo !== null) {
      return Number((fromToKrw * krwToTo).toFixed(8));
    }

    throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
  }

  private async getDirectOrInverseRate(from: string, to: string) {
    const direct = await this.exchangeRateRepository.findOne({
      where: { baseCurrency: from, targetCurrency: to },
    });

    if (direct) {
      return Number(direct.rate);
    }

    const inverse = await this.exchangeRateRepository.findOne({
      where: { baseCurrency: to, targetCurrency: from },
    });

    if (inverse) {
      return Number((1 / Number(inverse.rate)).toFixed(8));
    }

    return null;
  }

  private toTranslationDetail(item: Translation) {
    return {
      id: item.id,
      locale: item.locale,
      namespace: item.namespace,
      key: item.key,
      value: item.value,
      updatedAt: item.updatedAt,
    };
  }

  private toExchangeRateDetail(item: ExchangeRate) {
    return {
      id: item.id,
      baseCurrency: item.baseCurrency,
      targetCurrency: item.targetCurrency,
      rate: Number(item.rate),
      updatedAt: item.updatedAt,
    };
  }
}
