import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, In } from 'typeorm';
import { SpecDefinition } from './entities/spec-definition.entity';
import { ProductSpec } from './entities/product-spec.entity';
import { SpecScore } from './entities/spec-score.entity';
import { Product } from '../product/entities/product.entity';
import { CreateSpecDefinitionDto } from './dto/create-spec-definition.dto';
import { UpdateSpecDefinitionDto } from './dto/update-spec-definition.dto';
import { SetProductSpecsDto } from './dto/set-product-specs.dto';
import { CompareSpecsDto, ScoredCompareDto, SetSpecScoresDto } from './dto/compare-specs.dto';
import { BusinessException } from '../common/exceptions/business.exception';

@Injectable()
export class SpecService {
  constructor(
    @InjectRepository(SpecDefinition)
    private specDefRepository: Repository<SpecDefinition>,
    @InjectRepository(ProductSpec)
    private productSpecRepository: Repository<ProductSpec>,
    @InjectRepository(SpecScore)
    private specScoreRepository: Repository<SpecScore>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
  ) {}

  // ─── SPEC-01: 스펙 정의 목록 조회 ───
  async findDefinitions(categoryId?: number) {
    const where: any = {};
    if (categoryId) where.categoryId = categoryId;

    const defs = await this.specDefRepository.find({
      where,
      order: { sortOrder: 'ASC', name: 'ASC' },
    });

    return defs.map((d) => ({
      id: d.id,
      name: d.name,
      type: d.inputType,
      options: d.options,
      unit: d.unit,
      categoryId: d.categoryId,
    }));
  }

  // ─── SPEC-01: 스펙 정의 생성 ───
  async createDefinition(dto: CreateSpecDefinitionDto) {
    const def = this.specDefRepository.create({
      categoryId: dto.categoryId,
      name: dto.name,
      inputType: dto.inputType,
      dataType: dto.dataType,
      options: dto.options || null,
      unit: dto.unit || null,
    });
    const saved = await this.specDefRepository.save(def);
    return {
      id: saved.id,
      name: saved.name,
      type: saved.inputType,
      options: saved.options,
      unit: saved.unit,
      categoryId: saved.categoryId,
    };
  }

  // ─── SPEC-01: 스펙 정의 수정 ───
  async updateDefinition(id: number, dto: UpdateSpecDefinitionDto) {
    const def = await this.specDefRepository.findOne({ where: { id } });
    if (!def) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.name !== undefined) def.name = dto.name;
    if (dto.inputType !== undefined) def.inputType = dto.inputType;
    if (dto.dataType !== undefined) def.dataType = dto.dataType;
    if (dto.options !== undefined) def.options = dto.options || null;
    if (dto.unit !== undefined) def.unit = dto.unit || null;

    const saved = await this.specDefRepository.save(def);
    return {
      id: saved.id,
      name: saved.name,
      type: saved.inputType,
      options: saved.options,
      unit: saved.unit,
      categoryId: saved.categoryId,
    };
  }

  // ─── SPEC-01: 스펙 정의 삭제 ───
  async removeDefinition(id: number) {
    const def = await this.specDefRepository.findOne({ where: { id } });
    if (!def) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    await this.specDefRepository.remove(def);
    return { message: '스펙 정의가 삭제되었습니다.' };
  }

  // ─── SPEC-02: 상품 스펙 설정 ───
  async setProductSpecs(productId: number, dto: SetProductSpecsDto) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    // 기존 스펙 삭제 후 재등록
    await this.productSpecRepository.delete({ productId });

    const specs = dto.specs.map((s) =>
      this.productSpecRepository.create({
        productId,
        specDefinitionId: s.specDefinitionId,
        value: s.value,
        numericValue: s.numericValue ?? null,
      }),
    );

    const saved = await this.productSpecRepository.save(specs);
    return saved.map((s) => ({
      id: s.id,
      specDefinitionId: s.specDefinitionId,
      value: s.value,
      numericValue: s.numericValue,
    }));
  }

  // ─── SPEC-02: 상품 스펙 조회 ───
  async getProductSpecs(productId: number) {
    const specs = await this.productSpecRepository.find({
      where: { productId },
      relations: ['specDefinition'],
      order: { specDefinition: { sortOrder: 'ASC' } },
    });

    return specs.map((s) => ({
      id: s.id,
      name: s.specDefinition?.name,
      value: s.value,
      unit: s.specDefinition?.unit,
    }));
  }

  // ─── SPEC-04: 상품 스펙 비교 ───
  async compareSpecs(dto: CompareSpecsDto) {
    const products = await this.productRepository.find({
      where: { id: In(dto.productIds) },
    });

    const allSpecs = await this.productSpecRepository.find({
      where: { productId: In(dto.productIds) },
      relations: ['specDefinition'],
    });

    // 스펙 이름 기준 그룹핑
    const specNames = [...new Set(allSpecs.map((s) => s.specDefinition?.name).filter(Boolean))];

    const specs = specNames.map((name) => ({
      name,
      values: dto.productIds.map((pid) => {
        const spec = allSpecs.find(
          (s) => s.productId === pid && s.specDefinition?.name === name,
        );
        return spec ? spec.value : '-';
      }),
    }));

    return {
      products: dto.productIds.map((pid) => {
        const p = products.find((pr) => pr.id === pid)!;
        return {
          id: p.id,
          name: p.name,
          thumbnailUrl: p.thumbnailUrl,
          lowestPrice: p.lowestPrice,
        };
      }),
      specs,
    };
  }

  // ─── SPEC-04: 점수화 스펙 비교 ───
  async scoredCompare(dto: ScoredCompareDto) {
    const basic = await this.compareSpecs({ productIds: dto.productIds });

    // 점수 매핑 조회
    const allSpecs = await this.productSpecRepository.find({
      where: { productId: In(dto.productIds) },
      relations: ['specDefinition'],
    });

    const specDefIds = [...new Set(allSpecs.map((s) => s.specDefinitionId))];
    const scoreEntries = await this.specScoreRepository.find({
      where: { specDefinitionId: In(specDefIds) },
    });

    // 가중치 계산
    const specNames = basic.specs.map((s) => s.name);
    const equalWeight = specNames.length > 0 ? 100 / specNames.length : 0;
    const weights = dto.weights || {};

    const specScores = specNames.map((name) => {
      const weight = weights[name!] ?? equalWeight;
      const scores = dto.productIds.map((pid) => {
        const spec = allSpecs.find(
          (s) => s.productId === pid && s.specDefinition?.name === name,
        );
        if (!spec) return 0;

        const scoreEntry = scoreEntries.find(
          (se) => se.specDefinitionId === spec.specDefinitionId && se.value === spec.value,
        );
        return scoreEntry ? scoreEntry.score : 50; // 기본 50점
      });

      const weightedScores = scores.map((s) => Math.round((s * weight) / 100));
      const maxScore = Math.max(...scores);
      const winner = dto.productIds[scores.indexOf(maxScore)];

      return { name, scores, weightedScores, winner };
    });

    // 총점 계산
    const totalScores = dto.productIds.map((_, idx) =>
      specScores.reduce((sum, ss) => sum + ss.weightedScores[idx], 0),
    );

    // 순위
    const ranked = totalScores
      .map((score, idx) => ({ idx, score }))
      .sort((a, b) => b.score - a.score);

    const ranks = new Array(dto.productIds.length);
    ranked.forEach((r, rank) => { ranks[r.idx] = rank + 1; });

    const bestIdx = ranked[0].idx;
    const bestProduct = basic.products[bestIdx];

    return {
      products: basic.products.map((p, idx) => ({
        id: p.id,
        name: p.name,
        totalScore: totalScores[idx],
        rank: ranks[idx],
      })),
      specScores: specScores.map((ss) => ({
        name: ss.name,
        scores: ss.scores,
        winner: ss.winner,
      })),
      recommendation: `${bestProduct.name}이(가) 종합 점수가 가장 높습니다.`,
    };
  }

  // ─── 스펙 점수 매핑 설정 ───
  async setScores(specDefId: number, dto: SetSpecScoresDto) {
    const def = await this.specDefRepository.findOne({ where: { id: specDefId } });
    if (!def) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    // 기존 점수 삭제 후 재등록
    await this.specScoreRepository.delete({ specDefinitionId: specDefId });

    const scores = dto.scores.map((s) =>
      this.specScoreRepository.create({
        specDefinitionId: specDefId,
        value: s.value,
        score: s.score,
      }),
    );

    return this.specScoreRepository.save(scores);
  }
}
