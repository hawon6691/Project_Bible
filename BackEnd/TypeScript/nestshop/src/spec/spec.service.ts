import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { Category } from '../category/entities/category.entity';
import { Product } from '../product/entities/product.entity';
import { BusinessException } from '../common/exceptions/business.exception';
import { CompareSpecsDto, ScoredCompareDto, SetSpecScoresDto } from './dto/compare-specs.dto';
import { CreateSpecDefinitionDto } from './dto/create-spec-definition.dto';
import { SetProductSpecsDto } from './dto/set-product-specs.dto';
import { NumericCompareDto, ScoreByCategoryDto } from './dto/spec-engine.dto';
import { UpdateSpecDefinitionDto } from './dto/update-spec-definition.dto';
import { ProductSpec } from './entities/product-spec.entity';
import { SpecDataType, SpecDefinition } from './entities/spec-definition.entity';
import { SpecScore } from './entities/spec-score.entity';

@Injectable()
export class SpecService {
  constructor(
    @InjectRepository(SpecDefinition)
    private readonly specDefRepository: Repository<SpecDefinition>,
    @InjectRepository(ProductSpec)
    private readonly productSpecRepository: Repository<ProductSpec>,
    @InjectRepository(SpecScore)
    private readonly specScoreRepository: Repository<SpecScore>,
    @InjectRepository(Product)
    private readonly productRepository: Repository<Product>,
    @InjectRepository(Category)
    private readonly categoryRepository: Repository<Category>,
  ) {}

  async findDefinitions(categoryId?: number) {
    const where = categoryId ? { categoryId } : {};
    const definitions = await this.specDefRepository.find({
      where,
      order: { sortOrder: 'ASC', id: 'ASC' },
    });
    return definitions.map((item) => this.toDefinitionResponse(item));
  }

  // SENG-01: 카테고리 조상 체인을 따라 스펙 정의를 병합한다.
  async getResolvedDefinitions(categoryId: number) {
    const chain = await this.getCategoryChain(categoryId);
    const chainIds = chain.map((item) => item.id);

    const definitions = await this.specDefRepository.find({
      where: { categoryId: In(chainIds) },
      order: { sortOrder: 'ASC', id: 'ASC' },
    });

    const byKey = new Map<string, SpecDefinition>();
    for (const definition of definitions) {
      const key = definition.name.trim().toLowerCase();
      byKey.set(key, definition);
    }

    return {
      categoryChain: chain.map((item) => ({ id: item.id, name: item.name })),
      definitions: [...byKey.values()].map((item) => this.toDefinitionResponse(item)),
    };
  }

  async createDefinition(dto: CreateSpecDefinitionDto) {
    await this.ensureCategory(dto.categoryId);

    if (dto.parentDefinitionId) {
      await this.ensureSpecDefinition(dto.parentDefinitionId);
    }

    const definition = this.specDefRepository.create({
      categoryId: dto.categoryId,
      name: dto.name,
      type: dto.type,
      options: dto.options ?? null,
      unit: dto.unit ?? null,
      groupName: dto.groupName ?? null,
      parentDefinitionId: dto.parentDefinitionId ?? null,
      higherIsBetter: dto.higherIsBetter ?? true,
      isComparable: dto.isComparable ?? true,
      dataType: dto.dataType ?? SpecDataType.STRING,
      sortOrder: dto.sortOrder ?? 0,
    });

    const saved = await this.specDefRepository.save(definition);
    return this.toDefinitionResponse(saved);
  }

  async updateDefinition(id: number, dto: UpdateSpecDefinitionDto) {
    const definition = await this.ensureSpecDefinition(id);

    if (dto.parentDefinitionId !== undefined) {
      if (dto.parentDefinitionId === id) {
        throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '자기 자신을 부모로 지정할 수 없습니다.');
      }
      await this.ensureSpecDefinition(dto.parentDefinitionId);
      definition.parentDefinitionId = dto.parentDefinitionId;
    }

    if (dto.name !== undefined) definition.name = dto.name;
    if (dto.type !== undefined) definition.type = dto.type;
    if (dto.options !== undefined) definition.options = dto.options;
    if (dto.unit !== undefined) definition.unit = dto.unit;
    if (dto.groupName !== undefined) definition.groupName = dto.groupName;
    if (dto.higherIsBetter !== undefined) definition.higherIsBetter = dto.higherIsBetter;
    if (dto.isComparable !== undefined) definition.isComparable = dto.isComparable;
    if (dto.dataType !== undefined) definition.dataType = dto.dataType;
    if (dto.sortOrder !== undefined) definition.sortOrder = dto.sortOrder;

    const saved = await this.specDefRepository.save(definition);
    return this.toDefinitionResponse(saved);
  }

  async removeDefinition(id: number) {
    const definition = await this.ensureSpecDefinition(id);
    await this.specDefRepository.remove(definition);
    return { message: '스펙 정의가 삭제되었습니다.' };
  }

  async setProductSpecs(productId: number, dto: SetProductSpecsDto) {
    await this.ensureProduct(productId);

    const definitionIds = dto.specs.map((item) => item.specDefinitionId);
    const definitions = await this.specDefRepository.find({ where: { id: In(definitionIds) } });
    if (definitions.length !== new Set(definitionIds).size) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '일부 스펙 정의를 찾을 수 없습니다.');
    }

    await this.productSpecRepository.delete({ productId });

    const entities = dto.specs.map((item) =>
      this.productSpecRepository.create({
        productId,
        specDefinitionId: item.specDefinitionId,
        value: item.value,
        numericValue: item.numericValue ?? null,
      }),
    );

    const saved = await this.productSpecRepository.save(entities);
    return saved.map((item) => ({
      id: item.id,
      specDefinitionId: item.specDefinitionId,
      value: item.value,
      numericValue: item.numericValue,
    }));
  }

  async getProductSpecs(productId: number) {
    const specs = await this.productSpecRepository.find({
      where: { productId },
      relations: ['specDefinition'],
      order: { specDefinition: { sortOrder: 'ASC', id: 'ASC' } },
    });

    return specs.map((item) => ({
      id: item.id,
      specDefinitionId: item.specDefinitionId,
      name: item.specDefinition?.name,
      groupName: item.specDefinition?.groupName ?? '기타',
      value: item.value,
      numericValue: item.numericValue,
      unit: item.specDefinition?.unit,
      higherIsBetter: item.specDefinition?.higherIsBetter ?? true,
    }));
  }

  async getGroupedSpecs(productId: number) {
    const specs = await this.getProductSpecs(productId);
    const groups = new Map<string, typeof specs>();

    for (const item of specs) {
      const key = item.groupName ?? '기타';
      const prev = groups.get(key) ?? [];
      prev.push(item);
      groups.set(key, prev);
    }

    return {
      productId,
      groups: [...groups.entries()].map(([groupName, items]) => ({
        groupName,
        items,
      })),
    };
  }

  async compareSpecs(dto: CompareSpecsDto) {
    const products = await this.getProductsOrThrow(dto.productIds);
    const allSpecs = await this.getSpecsByProductIds(dto.productIds);

    const names = [...new Set(allSpecs.map((item) => item.specDefinition?.name).filter(Boolean))];
    const specs = names.map((name) => ({
      name,
      values: dto.productIds.map((productId) => {
        const spec = allSpecs.find(
          (item) => item.productId === productId && item.specDefinition?.name === name,
        );
        return spec?.value ?? '-';
      }),
    }));

    return {
      products: products.map((item) => this.toProductSummary(item)),
      specs,
    };
  }

  // SENG-03: 수치형 스펙을 비교해 best/worst를 함께 제공한다.
  async numericCompare(dto: NumericCompareDto) {
    const products = await this.getProductsOrThrow(dto.productIds);
    const allSpecs = await this.getSpecsByProductIds(dto.productIds);

    const numericDefinitions = [...new Set(allSpecs.map((item) => item.specDefinition))]
      .filter((definition): definition is SpecDefinition => !!definition)
      .filter((definition) => definition.dataType === SpecDataType.NUMBER && definition.isComparable);

    const rows = numericDefinitions.map((definition) => {
      const values = dto.productIds.map((productId) => {
        const spec = allSpecs.find(
          (item) => item.productId === productId && item.specDefinitionId === definition.id,
        );
        return spec?.numericValue !== null && spec?.numericValue !== undefined
          ? Number(spec.numericValue)
          : null;
      });

      const nonNull = values.filter((item): item is number => item !== null);
      if (!nonNull.length) {
        return {
          specDefinitionId: definition.id,
          name: definition.name,
          unit: definition.unit,
          higherIsBetter: definition.higherIsBetter,
          values,
          bestProductId: null,
          worstProductId: null,
        };
      }

      const bestValue = definition.higherIsBetter ? Math.max(...nonNull) : Math.min(...nonNull);
      const worstValue = definition.higherIsBetter ? Math.min(...nonNull) : Math.max(...nonNull);

      const bestIndex = values.findIndex((item) => item === bestValue);
      const worstIndex = values.findIndex((item) => item === worstValue);

      return {
        specDefinitionId: definition.id,
        name: definition.name,
        unit: definition.unit,
        higherIsBetter: definition.higherIsBetter,
        values,
        bestProductId: bestIndex >= 0 ? dto.productIds[bestIndex] : null,
        worstProductId: worstIndex >= 0 ? dto.productIds[worstIndex] : null,
      };
    });

    return {
      products: products.map((item) => this.toProductSummary(item)),
      specs: rows,
    };
  }

  async scoredCompare(dto: ScoredCompareDto) {
    const base = await this.compareSpecs({ productIds: dto.productIds });
    const allSpecs = await this.getSpecsByProductIds(dto.productIds);
    const scoreMap = await this.getScoreMap(allSpecs.map((item) => item.specDefinitionId));
    const weights = dto.weights ?? {};
    const hasWeights = Object.keys(weights).length > 0;

    const specScores = base.specs.map((specRow) => {
      const rowScores = dto.productIds.map((productId) => {
        const productSpec = allSpecs.find(
          (item) => item.productId === productId && item.specDefinition?.name === specRow.name,
        );
        if (!productSpec) return 0;

        const mapped = scoreMap.get(`${productSpec.specDefinitionId}:${productSpec.value}`);
        if (mapped !== undefined) return mapped;

        if (productSpec.numericValue !== null && productSpec.numericValue !== undefined) {
          return Number(productSpec.numericValue);
        }

        return 0;
      });

      const maxScore = Math.max(...rowScores);
      const winner = maxScore > 0 ? dto.productIds[rowScores.indexOf(maxScore)] : null;

      return {
        name: specRow.name,
        scores: rowScores,
        winner,
      };
    });

    const totals = dto.productIds.map((productId, index) => {
      const baseProduct = base.products[index];
      let totalScore = 0;
      let weightSum = 0;

      for (const row of specScores) {
        const weight = hasWeights ? Number(weights[row.name] ?? 0) : 1;
        totalScore += row.scores[index] * weight;
        weightSum += weight;
      }

      return {
        id: productId,
        name: baseProduct?.name ?? `상품 ${productId}`,
        thumbnailUrl: baseProduct?.thumbnailUrl ?? null,
        lowestPrice: baseProduct?.lowestPrice ?? null,
        totalScore: weightSum > 0 ? Math.round(totalScore / weightSum) : 0,
        rank: 0,
      };
    });

    totals.sort((a, b) => b.totalScore - a.totalScore).forEach((item, idx) => {
      item.rank = idx + 1;
    });

    const winner = totals[0];

    return {
      products: totals,
      specScores,
      recommendation: winner ? `${winner.name}이(가) 종합 점수가 가장 높습니다.` : '추천 결과가 없습니다.',
    };
  }

  // SENG-04: 카테고리 기준 스펙 점수(가중치 없음) 계산
  async scoreByCategory(dto: ScoreByCategoryDto) {
    const resolved = await this.getResolvedDefinitions(dto.categoryId);
    const comparableNames = new Set(
      resolved.definitions.filter((item) => item.isComparable).map((item) => item.name),
    );

    const scored = await this.scoredCompare({
      productIds: dto.productIds,
      weights: Object.fromEntries([...comparableNames].map((name) => [name, 1])),
    });

    return {
      categoryId: dto.categoryId,
      products: scored.products,
      recommendation: scored.recommendation,
    };
  }

  async setSpecScores(specDefId: number, dto: SetSpecScoresDto) {
    await this.ensureSpecDefinition(specDefId);

    await this.specScoreRepository.delete({ specDefinitionId: specDefId });
    const entities = dto.scores.map((item) =>
      this.specScoreRepository.create({
        specDefinitionId: specDefId,
        value: item.value,
        score: item.score,
        benchmarkSource: item.benchmarkSource ?? null,
      }),
    );

    const saved = await this.specScoreRepository.save(entities);
    return saved.map((item) => ({
      id: item.id,
      value: item.value,
      score: item.score,
      benchmarkSource: item.benchmarkSource,
    }));
  }

  // SENG-05: 기준 상품과의 스펙 유사도 기반 대안 상품 추천
  async findSimilarProducts(productId: number, limit: number) {
    await this.ensureProduct(productId);

    const baseSpecs = await this.productSpecRepository.find({
      where: { productId },
      relations: ['specDefinition'],
    });

    if (!baseSpecs.length) {
      return { items: [] };
    }

    const candidates = await this.productRepository.find({
      where: { id: In((await this.productRepository.find()).map((item) => item.id)) },
    });

    const candidateIds = candidates.map((item) => item.id).filter((id) => id !== productId);
    if (!candidateIds.length) {
      return { items: [] };
    }

    const candidateSpecs = await this.productSpecRepository.find({
      where: { productId: In(candidateIds) },
      relations: ['specDefinition'],
    });

    const baseByDef = new Map<number, ProductSpec>();
    for (const spec of baseSpecs) {
      baseByDef.set(spec.specDefinitionId, spec);
    }

    const byProduct = new Map<number, ProductSpec[]>();
    for (const spec of candidateSpecs) {
      const prev = byProduct.get(spec.productId) ?? [];
      prev.push(spec);
      byProduct.set(spec.productId, prev);
    }

    const results = candidateIds.map((candidateId) => {
      const specs = byProduct.get(candidateId) ?? [];
      if (!specs.length) {
        return { productId: candidateId, similarity: 0 };
      }

      let score = 0;
      let count = 0;

      for (const spec of specs) {
        const base = baseByDef.get(spec.specDefinitionId);
        if (!base) continue;
        count += 1;

        if (
          base.numericValue !== null &&
          base.numericValue !== undefined &&
          spec.numericValue !== null &&
          spec.numericValue !== undefined
        ) {
          const a = Number(base.numericValue);
          const b = Number(spec.numericValue);
          const ratio = a === 0 && b === 0 ? 1 : 1 - Math.min(1, Math.abs(a - b) / Math.max(Math.abs(a), 1));
          score += ratio;
          continue;
        }

        if (base.value === spec.value) {
          score += 1;
        }
      }

      const similarity = count > 0 ? Number(((score / count) * 100).toFixed(2)) : 0;
      return { productId: candidateId, similarity };
    });

    const top = results.sort((a, b) => b.similarity - a.similarity).slice(0, limit);
    const productMap = new Map(candidates.map((item) => [item.id, item]));

    return {
      items: top.map((item) => {
        const product = productMap.get(item.productId)!;
        return {
          ...this.toProductSummary(product),
          similarity: item.similarity,
        };
      }),
    };
  }

  private async getProductsOrThrow(productIds: number[]) {
    const unique = [...new Set(productIds)];
    const products = await this.productRepository.find({ where: { id: In(unique) } });
    if (products.length !== unique.length) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return unique.map((id) => products.find((item) => item.id === id)!);
  }

  private async getSpecsByProductIds(productIds: number[]) {
    return this.productSpecRepository.find({
      where: { productId: In(productIds) },
      relations: ['specDefinition'],
    });
  }

  private async getScoreMap(specDefinitionIds: number[]) {
    const uniqueIds = [...new Set(specDefinitionIds)];
    if (!uniqueIds.length) {
      return new Map<string, number>();
    }

    const scores = await this.specScoreRepository.find({
      where: { specDefinitionId: In(uniqueIds) },
    });

    const scoreMap = new Map<string, number>();
    for (const item of scores) {
      scoreMap.set(`${item.specDefinitionId}:${item.value}`, item.score);
    }

    return scoreMap;
  }

  private async getCategoryChain(categoryId: number) {
    const categories = await this.categoryRepository.find();
    const byId = new Map(categories.map((item) => [item.id, item]));
    const chain: Category[] = [];

    let cursor = byId.get(categoryId);
    if (!cursor) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '카테고리를 찾을 수 없습니다.');
    }

    while (cursor) {
      chain.unshift(cursor);
      cursor = cursor.parentId ? byId.get(cursor.parentId) : undefined;
    }

    return chain;
  }

  private async ensureProduct(productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return product;
  }

  private async ensureCategory(categoryId: number) {
    const category = await this.categoryRepository.findOne({ where: { id: categoryId } });
    if (!category) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '카테고리를 찾을 수 없습니다.');
    }

    return category;
  }

  private async ensureSpecDefinition(id: number) {
    const definition = await this.specDefRepository.findOne({ where: { id } });
    if (!definition) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return definition;
  }

  private toDefinitionResponse(item: SpecDefinition) {
    return {
      id: item.id,
      categoryId: item.categoryId,
      name: item.name,
      type: item.type,
      options: item.options,
      unit: item.unit,
      groupName: item.groupName,
      parentDefinitionId: item.parentDefinitionId,
      higherIsBetter: item.higherIsBetter,
      isComparable: item.isComparable,
      dataType: item.dataType,
      sortOrder: item.sortOrder,
    };
  }

  private toProductSummary(item: Product) {
    return {
      id: item.id,
      name: item.name,
      thumbnailUrl: item.thumbnailUrl,
      lowestPrice: item.lowestPrice,
    };
  }
}
