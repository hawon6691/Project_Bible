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
    const definitions = await this.specDefRepository.find({
      where,
      order: { sortOrder: 'ASC', id: 'ASC' },
    });

    return definitions.map((d) => ({
      id: d.id,
      categoryId: d.categoryId,
      name: d.name,
      type: d.type,
      options: d.options,
      unit: d.unit,
      isComparable: d.isComparable,
      dataType: d.dataType,
      sortOrder: d.sortOrder,
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
    const definition = this.specDefRepository.create({
      categoryId: dto.categoryId,
      name: dto.name,
      type: dto.type,
      options: dto.options || null,
      unit: dto.unit || null,
      isComparable: dto.isComparable ?? true,
      dataType: dto.dataType,
      sortOrder: dto.sortOrder || 0,
    });

    const saved = await this.specDefRepository.save(definition);
    return this.toDefinitionResponse(saved);
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
    const definition = await this.specDefRepository.findOne({ where: { id } });
    if (!definition) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.name !== undefined) definition.name = dto.name;
    if (dto.type !== undefined) definition.type = dto.type;
    if (dto.options !== undefined) definition.options = dto.options;
    if (dto.unit !== undefined) definition.unit = dto.unit;
    if (dto.isComparable !== undefined) definition.isComparable = dto.isComparable;
    if (dto.dataType !== undefined) definition.dataType = dto.dataType;
    if (dto.sortOrder !== undefined) definition.sortOrder = dto.sortOrder;

    const saved = await this.specDefRepository.save(definition);
    return this.toDefinitionResponse(saved);
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
    const definition = await this.specDefRepository.findOne({ where: { id } });
    if (!definition) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.specDefRepository.remove(definition);
    return { message: '스펙 정의가 삭제되었습니다.' };
  }

  // ─── SPEC-02: 상품 스펙 설정 (PUT) ───
  async setProductSpecs(productId: number, dto: SetProductSpecsDto) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    // 기존 스펙 삭제 후 재등록
    // 기존 스펙 전부 삭제 후 재생성
    await this.productSpecRepository.delete({ productId });

    const specs = dto.specs.map((s) =>
      this.productSpecRepository.create({
        productId,
        specDefinitionId: s.specDefinitionId,
        value: s.value,
        numericValue: s.numericValue ?? null,
        numericValue: s.numericValue || null,
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
      numericValue: s.numericValue,
      unit: s.specDefinition?.unit,
    }));
  }

  // ─── SPEC-04: 상품 스펙 비교 ───
  async compareSpecs(dto: CompareSpecsDto) {
    const products = await this.productRepository.find({
      where: { id: In(dto.productIds) },
    });

    if (products.length !== dto.productIds.length) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    // 모든 상품의 스펙을 한번에 조회
    const allSpecs = await this.productSpecRepository.find({
      where: { productId: In(dto.productIds) },
      relations: ['specDefinition'],
    });

    // 스펙 이름 기준 그룹핑
    // 스펙 이름별 그룹핑
    const specNames = [...new Set(allSpecs.map((s) => s.specDefinition?.name).filter(Boolean))];

    const specs = specNames.map((name) => ({
      name,
      values: dto.productIds.map((pid) => {
        const spec = allSpecs.find(
          (s) => s.productId === pid && s.specDefinition?.name === name,
        );
        return spec ? spec.value : '-';
        return spec?.value || '-';
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
    const compareResult = await this.compareSpecs({ productIds: dto.productIds });

    // 각 스펙의 점수 조회
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
    const scores = await this.specScoreRepository.find({
      where: { specDefinitionId: In(specDefIds) },
    });

    const scoreMap = new Map<string, number>();
    for (const s of scores) {
      scoreMap.set(`${s.specDefinitionId}:${s.value}`, s.score);
    }

    // 가중치 계산
    const weights = dto.weights || {};
    const specNames = compareResult.specs.map((s) => s.name);
    const hasWeights = Object.keys(weights).length > 0;

    const specScores = specNames.map((name) => {
      const specScoreValues = dto.productIds.map((pid) => {
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
        return scoreMap.get(`${spec.specDefinitionId}:${spec.value}`) || 0;
      });

      const maxScore = Math.max(...specScoreValues);
      const winner = maxScore > 0
        ? dto.productIds[specScoreValues.indexOf(maxScore)]
        : null;

      return { name, scores: specScoreValues, winner };
    });

    // 총점 계산
    const productScores = dto.productIds.map((pid, pidIdx) => {
      let total = 0;
      let weightSum = 0;

      specScores.forEach((ss) => {
        const w = hasWeights ? (weights[ss.name] || 0) : 1;
        total += ss.scores[pidIdx] * w;
        weightSum += w;
      });

      return {
        id: compareResult.products[pidIdx].id,
        name: compareResult.products[pidIdx].name,
        totalScore: weightSum > 0 ? Math.round(total / weightSum) : 0,
        rank: 0,
      };
    });

    // 순위 부여
    productScores
      .sort((a, b) => b.totalScore - a.totalScore)
      .forEach((p, i) => { p.rank = i + 1; });

    const winner = productScores[0];

    return {
      products: productScores,
      specScores,
      recommendation: `${winner.name}이(가) 종합 점수가 가장 높습니다.`,
    };
  }

  // ─── 스펙 점수 매핑 설정 ───
  async setSpecScores(specDefId: number, dto: SetSpecScoresDto) {
    const definition = await this.specDefRepository.findOne({ where: { id: specDefId } });
    if (!definition) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    // 기존 점수 삭제 후 재생성
    await this.specScoreRepository.delete({ specDefinitionId: specDefId });

    const entities = dto.scores.map((s) =>
      this.specScoreRepository.create({
        specDefinitionId: specDefId,
        value: s.value,
        score: s.score,
      }),
    );

    return this.specScoreRepository.save(scores);
        benchmarkSource: s.benchmarkSource || null,
      }),
    );

    const saved = await this.specScoreRepository.save(entities);
    return saved.map((s) => ({
      id: s.id,
      value: s.value,
      score: s.score,
      benchmarkSource: s.benchmarkSource,
    }));
  }

  // ─── 헬퍼 ───
  private toDefinitionResponse(d: SpecDefinition) {
    return {
      id: d.id,
      categoryId: d.categoryId,
      name: d.name,
      type: d.type,
      options: d.options,
      unit: d.unit,
      isComparable: d.isComparable,
      dataType: d.dataType,
      sortOrder: d.sortOrder,
    };
  }
}
