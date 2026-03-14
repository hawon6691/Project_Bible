import { HttpStatus, Injectable } from '@nestjs/common';
import { BusinessException } from '../common/exceptions/business.exception';
import { AutoEstimateDto } from './dto/auto-estimate.dto';
import { CarModelQueryDto } from './dto/car-model-query.dto';

type CarModel = {
  id: number;
  brand: string;
  name: string;
  type: 'SEDAN' | 'SUV' | 'EV';
  basePrice: number;
};

type CarTrim = {
  id: number;
  modelId: number;
  name: string;
  priceDelta: number;
};

type CarOption = {
  id: number;
  trimId: number;
  name: string;
  price: number;
};

@Injectable()
export class AutoService {
  // 예시 데이터: 실제 운영에서는 제조사 API/마스터 테이블로 대체한다.
  private readonly models: CarModel[] = [
    { id: 1, brand: 'Hyundai', name: 'IONIQ 6', type: 'EV', basePrice: 52000000 },
    { id: 2, brand: 'Kia', name: 'Sorento', type: 'SUV', basePrice: 42000000 },
    { id: 3, brand: 'Genesis', name: 'G80', type: 'SEDAN', basePrice: 61000000 },
  ];

  private readonly trims: CarTrim[] = [
    { id: 1, modelId: 1, name: 'E-LITE', priceDelta: 0 },
    { id: 2, modelId: 1, name: 'E-PLUS', priceDelta: 4500000 },
    { id: 3, modelId: 2, name: '2.2 Diesel', priceDelta: 0 },
    { id: 4, modelId: 2, name: 'Hybrid', priceDelta: 3800000 },
    { id: 5, modelId: 3, name: '2.5 Turbo', priceDelta: 0 },
    { id: 6, modelId: 3, name: '3.5 Turbo', priceDelta: 7000000 },
  ];

  private readonly options: CarOption[] = [
    { id: 1, trimId: 1, name: 'Heat Pump', price: 1200000 },
    { id: 2, trimId: 2, name: 'HUD Package', price: 1800000 },
    { id: 3, trimId: 3, name: 'Drive Wise', price: 1500000 },
    { id: 4, trimId: 4, name: 'Panorama Sunroof', price: 900000 },
    { id: 5, trimId: 5, name: 'Lexicon Sound', price: 1600000 },
    { id: 6, trimId: 6, name: 'Rear Seat Comfort', price: 2400000 },
  ];

  private readonly leaseOffers = [
    { modelId: 1, provider: 'LeaseOne', months: 48, monthlyFee: 790000, downPayment: 5000000 },
    { modelId: 1, provider: 'RentCarX', months: 60, monthlyFee: 730000, downPayment: 3000000 },
    { modelId: 2, provider: 'LeaseOne', months: 48, monthlyFee: 620000, downPayment: 4000000 },
    { modelId: 3, provider: 'PremiumLease', months: 48, monthlyFee: 980000, downPayment: 7000000 },
  ];

  getModels(query: CarModelQueryDto) {
    return this.models.filter((model) => {
      const brandMatch = query.brand ? model.brand.toLowerCase() === query.brand.toLowerCase() : true;
      const typeMatch = query.type ? model.type.toLowerCase() === query.type.toLowerCase() : true;
      return brandMatch && typeMatch;
    });
  }

  getTrims(modelId: number) {
    this.ensureModel(modelId);

    const trims = this.trims.filter((trim) => trim.modelId === modelId);
    return trims.map((trim) => ({
      ...trim,
      options: this.options.filter((option) => option.trimId === trim.id),
    }));
  }

  estimate(dto: AutoEstimateDto) {
    const model = this.ensureModel(dto.modelId);
    const trim = this.trims.find((item) => item.id === dto.trimId && item.modelId === dto.modelId);

    if (!trim) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '해당 모델의 트림을 찾을 수 없습니다.');
    }

    const selectedOptions = this.options.filter((option) => option.trimId === trim.id && dto.optionIds.includes(option.id));
    const optionPrice = selectedOptions.reduce((sum, option) => sum + option.price, 0);

    const basePrice = model.basePrice + trim.priceDelta;
    const tax = Math.round((basePrice + optionPrice) * 0.07);
    const totalPrice = basePrice + optionPrice + tax;

    // 단순 월 납입금 추정: 선수금 10%, 60개월 균등 분할
    const downPayment = Math.round(totalPrice * 0.1);
    const monthlyPayment = Math.round((totalPrice - downPayment) / 60);

    return {
      modelId: model.id,
      trimId: trim.id,
      selectedOptionIds: dto.optionIds,
      basePrice,
      optionPrice,
      tax,
      totalPrice,
      monthlyPayment,
    };
  }

  getLeaseOffers(modelId: number) {
    this.ensureModel(modelId);

    return this.leaseOffers.filter((offer) => offer.modelId === modelId);
  }

  private ensureModel(modelId: number) {
    const model = this.models.find((item) => item.id === modelId);
    if (!model) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '자동차 모델을 찾을 수 없습니다.');
    }
    return model;
  }
}
