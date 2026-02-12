import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Product } from './entities/product.entity';
import { ProductOption } from './entities/product-option.entity';
import { ProductImage } from './entities/product-image.entity';
import { CreateProductDto } from './dto/create-product.dto';
import { UpdateProductDto } from './dto/update-product.dto';
import { ProductQueryDto, ProductSort } from './dto/product-query.dto';
import { CreateProductOptionDto } from './dto/create-product.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PaginationResponseDto } from '../common/dto/pagination.dto';

@Injectable()
export class ProductService {
  constructor(
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
    @InjectRepository(ProductOption)
    private optionRepository: Repository<ProductOption>,
    @InjectRepository(ProductImage)
    private imageRepository: Repository<ProductImage>,
  ) {}

  // ─── PROD-01: 상품 목록 조회 ───
  async findAll(query: ProductQueryDto) {
    const qb = this.productRepository
      .createQueryBuilder('p')
      .where('p.deletedAt IS NULL');

    if (query.categoryId) {
      qb.andWhere('p.categoryId = :categoryId', { categoryId: query.categoryId });
    }
    if (query.search) {
      qb.andWhere('p.name ILIKE :search', { search: `%${query.search}%` });
    }
    if (query.minPrice !== undefined) {
      qb.andWhere('p.lowestPrice >= :minPrice', { minPrice: query.minPrice });
    }
    if (query.maxPrice !== undefined) {
      qb.andWhere('p.lowestPrice <= :maxPrice', { maxPrice: query.maxPrice });
    }

    // 스펙 필터
    if (query.specs) {
      try {
        const specFilters: Record<string, string> = JSON.parse(query.specs);
        let specIdx = 0;
        for (const [specName, specValue] of Object.entries(specFilters)) {
          const alias = `ps${specIdx}`;
          const sdAlias = `sd${specIdx}`;
          qb.innerJoin('product_specs', alias, `${alias}.product_id = p.id`)
            .innerJoin('spec_definitions', sdAlias, `${sdAlias}.id = ${alias}.spec_definition_id`)
            .andWhere(`${sdAlias}.name = :sn${specIdx}`, { [`sn${specIdx}`]: specName })
            .andWhere(`${alias}.value = :sv${specIdx}`, { [`sv${specIdx}`]: specValue });
          specIdx++;
        }
      } catch {
        // 잘못된 JSON은 무시
      }
    }

    const sort = query.sort || ProductSort.NEWEST;
    this.applySort(qb, sort);

    const totalItems = await qb.getCount();
    const limit = query.limit || 20;
    const items = await qb.skip(query.skip).take(limit).getMany();

    const data = items.map((p) => ({
      id: p.id,
      name: p.name,
      lowestPrice: p.lowestPrice,
      sellerCount: p.sellerCount,
      thumbnailUrl: p.thumbnailUrl,
      reviewCount: p.reviewCount,
      averageRating: Number(p.averageRating),
      createdAt: p.createdAt,
    }));

    return new PaginationResponseDto(data, totalItems, query.page, limit);
  }

  // ─── PROD-02: 상품 상세 조회 ───
  async findOne(id: number) {
    const product = await this.productRepository.findOne({
      where: { id },
      relations: ['category', 'options', 'images'],
    });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    // 조회수 증가
    await this.productRepository.increment({ id }, 'viewCount', 1);

    return {
      id: product.id,
      name: product.name,
      description: product.description,
      price: product.price,
      discountPrice: product.discountPrice,
      lowestPrice: product.lowestPrice,
      stock: product.stock,
      status: product.status,
      category: product.category
        ? { id: product.category.id, name: product.category.name }
        : null,
      options: product.options?.map((o) => ({
        id: o.id, name: o.name, values: o.values,
      })) || [],
      images: product.images?.map((i) => ({
        id: i.id, url: i.url, isMain: i.isMain, sortOrder: i.sortOrder,
      })) || [],
      sellerCount: product.sellerCount,
      reviewCount: product.reviewCount,
      averageRating: Number(product.averageRating),
      salesCount: product.salesCount,
      createdAt: product.createdAt,
    };
  }

  // ─── PROD-03: 상품 등록 ───
  async create(dto: CreateProductDto) {
    const product = this.productRepository.create({
      name: dto.name,
      description: dto.description,
      price: dto.price,
      discountPrice: dto.discountPrice || null,
      stock: dto.stock,
      categoryId: dto.categoryId,
      status: dto.status,
      thumbnailUrl: dto.thumbnailUrl || null,
      lowestPrice: dto.price,
    });
    const saved = await this.productRepository.save(product);

    // 옵션 저장
    if (dto.options?.length) {
      const options = dto.options.map((o) =>
        this.optionRepository.create({
          productId: saved.id,
          name: o.name,
          values: o.values,
        }),
      );
      await this.optionRepository.save(options);
    }

    // 이미지 저장
    if (dto.images?.length) {
      const images = dto.images.map((i) =>
        this.imageRepository.create({
          productId: saved.id,
          url: i.url,
          isMain: i.isMain || false,
          sortOrder: i.sortOrder || 0,
        }),
      );
      await this.imageRepository.save(images);
    }

    return this.findOne(saved.id);
  }

  // ─── PROD-04: 상품 수정 ───
  async update(id: number, dto: UpdateProductDto) {
    const product = await this.productRepository.findOne({ where: { id } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.name !== undefined) product.name = dto.name;
    if (dto.description !== undefined) product.description = dto.description;
    if (dto.price !== undefined) product.price = dto.price;
    if (dto.discountPrice !== undefined) product.discountPrice = dto.discountPrice;
    if (dto.stock !== undefined) product.stock = dto.stock;
    if (dto.categoryId !== undefined) product.categoryId = dto.categoryId;
    if (dto.status !== undefined) product.status = dto.status;
    if (dto.thumbnailUrl !== undefined) product.thumbnailUrl = dto.thumbnailUrl;

    await this.productRepository.save(product);
    return this.findOne(id);
  }

  // ─── PROD-05: 상품 삭제 (소프트 삭제) ───
  async remove(id: number) {
    const product = await this.productRepository.findOne({ where: { id } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    await this.productRepository.softRemove(product);
    return { message: '상품이 삭제되었습니다.' };
  }

  // ─── PROD-06: 옵션 추가 ───
  async addOption(productId: number, dto: CreateProductOptionDto) {
    await this.ensureProductExists(productId);
    const option = this.optionRepository.create({
      productId,
      name: dto.name,
      values: dto.values,
    });
    return this.optionRepository.save(option);
  }

  // ─── PROD-06: 옵션 수정 ───
  async updateOption(productId: number, optionId: number, dto: CreateProductOptionDto) {
    await this.ensureProductExists(productId);
    const option = await this.optionRepository.findOne({
      where: { id: optionId, productId },
    });
    if (!option) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    option.name = dto.name;
    option.values = dto.values;
    return this.optionRepository.save(option);
  }

  // ─── PROD-06: 옵션 삭제 ───
  async removeOption(productId: number, optionId: number) {
    await this.ensureProductExists(productId);
    const option = await this.optionRepository.findOne({
      where: { id: optionId, productId },
    });
    if (!option) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    await this.optionRepository.remove(option);
    return { message: '옵션이 삭제되었습니다.' };
  }

  // ─── PROD-07: 이미지 추가 ───
  async addImage(productId: number, url: string, isMain = false, sortOrder = 0) {
    await this.ensureProductExists(productId);

    // 대표 이미지 지정 시 기존 대표 해제
    if (isMain) {
      await this.imageRepository.update(
        { productId, isMain: true },
        { isMain: false },
      );
    }

    const image = this.imageRepository.create({ productId, url, isMain, sortOrder });
    return this.imageRepository.save(image);
  }

  // ─── PROD-07: 이미지 삭제 ───
  async removeImage(productId: number, imageId: number) {
    await this.ensureProductExists(productId);
    const image = await this.imageRepository.findOne({
      where: { id: imageId, productId },
    });
    if (!image) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    await this.imageRepository.remove(image);
    return { message: '이미지가 삭제되었습니다.' };
  }

  private async ensureProductExists(id: number) {
    const exists = await this.productRepository.findOne({ where: { id } });
    if (!exists) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
  }

  private applySort(qb: any, sort: ProductSort) {
    switch (sort) {
      case ProductSort.POPULARITY:
        qb.orderBy('p.popularityScore', 'DESC');
        break;
      case ProductSort.PRICE_ASC:
        qb.orderBy('p.lowestPrice', 'ASC', 'NULLS LAST');
        break;
      case ProductSort.PRICE_DESC:
        qb.orderBy('p.lowestPrice', 'DESC', 'NULLS LAST');
        break;
      case ProductSort.RATING_DESC:
        qb.orderBy('p.averageRating', 'DESC').addOrderBy('p.reviewCount', 'DESC');
        break;
      case ProductSort.RATING_ASC:
        qb.orderBy('p.averageRating', 'ASC');
        break;
      case ProductSort.NEWEST:
      default:
        qb.orderBy('p.createdAt', 'DESC');
        break;
    }
  }
}
