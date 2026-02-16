import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { Product } from '../product/entities/product.entity';
import { CreateNewsCategoryDto } from './dto/create-news-category.dto';
import { CreateNewsDto } from './dto/create-news.dto';
import { NewsListQueryDto } from './dto/news-list-query.dto';
import { UpdateNewsDto } from './dto/update-news.dto';
import { NewsCategory } from './entities/news-category.entity';
import { NewsProduct } from './entities/news-product.entity';
import { News } from './entities/news.entity';

@Injectable()
export class NewsService {
  constructor(
    @InjectRepository(News)
    private newsRepository: Repository<News>,
    @InjectRepository(NewsCategory)
    private newsCategoryRepository: Repository<NewsCategory>,
    @InjectRepository(NewsProduct)
    private newsProductRepository: Repository<NewsProduct>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
  ) {}

  async getCategories() {
    const categories = await this.newsCategoryRepository.find({ order: { id: 'ASC' } });
    return categories.map((item) => this.toCategoryDetail(item));
  }

  async createCategory(dto: CreateNewsCategoryDto) {
    const exists = await this.newsCategoryRepository.findOne({
      where: [{ name: dto.name }, { slug: dto.slug }],
    });

    if (exists) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.CONFLICT, '이미 존재하는 카테고리입니다.');
    }

    const category = this.newsCategoryRepository.create({
      name: dto.name,
      slug: dto.slug,
    });

    const saved = await this.newsCategoryRepository.save(category);
    return this.toCategoryDetail(saved);
  }

  async removeCategory(categoryId: number) {
    const category = await this.newsCategoryRepository.findOne({ where: { id: categoryId } });
    if (!category) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const newsCount = await this.newsRepository.count({ where: { categoryId } });
    if (newsCount > 0) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '게시된 뉴스가 있어 삭제할 수 없습니다.');
    }

    await this.newsCategoryRepository.softDelete({ id: categoryId });
    return { success: true, message: '뉴스 카테고리가 삭제되었습니다.' };
  }

  async getNewsList(query: NewsListQueryDto) {
    const qb = this.newsRepository
      .createQueryBuilder('news')
      .leftJoinAndSelect('news.category', 'category')
      .orderBy('news.createdAt', 'DESC')
      .skip(query.skip)
      .take(query.limit);

    if (query.category) {
      qb.andWhere('(category.slug = :category OR category.name = :category)', { category: query.category });
    }

    const [items, total] = await qb.getManyAndCount();
    const mapped = items.map((item) => this.toNewsSummary(item));

    return new PaginationResponseDto(mapped, total, query.page, query.limit);
  }

  async getNewsDetail(newsId: number) {
    const news = await this.newsRepository.findOne({
      where: { id: newsId },
      relations: { category: true },
    });

    if (!news) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.newsRepository.increment({ id: newsId }, 'viewCount', 1);

    const productLinks = await this.newsProductRepository.find({ where: { newsId } });
    const productIds = productLinks.map((item) => item.productId);
    const relatedProducts = productIds.length
      ? await this.productRepository.find({ where: { id: In(productIds) } })
      : [];

    return {
      ...this.toNewsDetail(news),
      relatedProducts: relatedProducts.map((product) => ({
        id: product.id,
        name: product.name,
        thumbnailUrl: product.thumbnailUrl,
        lowestPrice: product.lowestPrice,
      })),
    };
  }

  // 뉴스 작성 시 카테고리/연관상품 유효성을 함께 검증한다.
  async createNews(dto: CreateNewsDto) {
    await this.ensureCategory(dto.categoryId);
    await this.ensureProducts(dto.productIds ?? []);

    const news = this.newsRepository.create({
      title: dto.title,
      content: dto.content,
      categoryId: dto.categoryId,
      thumbnailUrl: dto.thumbnailUrl ?? null,
      viewCount: 0,
    });

    const saved = await this.newsRepository.save(news);
    await this.syncNewsProducts(saved.id, dto.productIds ?? []);

    return this.getNewsDetail(saved.id);
  }

  async updateNews(newsId: number, dto: UpdateNewsDto) {
    const news = await this.newsRepository.findOne({ where: { id: newsId } });
    if (!news) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.categoryId !== undefined) {
      await this.ensureCategory(dto.categoryId);
      news.categoryId = dto.categoryId;
    }

    if (dto.title !== undefined) news.title = dto.title;
    if (dto.content !== undefined) news.content = dto.content;
    if (dto.thumbnailUrl !== undefined) news.thumbnailUrl = dto.thumbnailUrl ?? null;

    await this.newsRepository.save(news);

    if (dto.productIds !== undefined) {
      await this.ensureProducts(dto.productIds);
      await this.syncNewsProducts(newsId, dto.productIds);
    }

    return this.getNewsDetail(newsId);
  }

  async removeNews(newsId: number) {
    const news = await this.newsRepository.findOne({ where: { id: newsId } });
    if (!news) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.newsProductRepository.softDelete({ newsId });
    await this.newsRepository.softDelete({ id: newsId });

    return { success: true, message: '뉴스가 삭제되었습니다.' };
  }

  private async ensureCategory(categoryId: number) {
    const category = await this.newsCategoryRepository.findOne({ where: { id: categoryId } });
    if (!category) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND, '카테고리를 찾을 수 없습니다.');
    }
    return category;
  }

  private async ensureProducts(productIds: number[]) {
    if (!productIds.length) return;

    const products = await this.productRepository.find({ where: { id: In(productIds) } });
    if (products.length !== new Set(productIds).size) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
  }

  private async syncNewsProducts(newsId: number, productIds: number[]) {
    await this.newsProductRepository.softDelete({ newsId });

    if (!productIds.length) {
      return;
    }

    const uniqueIds = [...new Set(productIds)];
    const rows = uniqueIds.map((productId) =>
      this.newsProductRepository.create({
        newsId,
        productId,
      }),
    );

    await this.newsProductRepository.save(rows);
  }

  private toCategoryDetail(item: NewsCategory) {
    return {
      id: item.id,
      name: item.name,
      slug: item.slug,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    };
  }

  private toNewsSummary(item: News) {
    return {
      id: item.id,
      title: item.title,
      thumbnailUrl: item.thumbnailUrl,
      category: item.category
        ? {
            id: item.category.id,
            name: item.category.name,
            slug: item.category.slug,
          }
        : null,
      viewCount: item.viewCount,
      createdAt: item.createdAt,
    };
  }

  private toNewsDetail(item: News) {
    return {
      id: item.id,
      title: item.title,
      content: item.content,
      thumbnailUrl: item.thumbnailUrl,
      category: item.category
        ? {
            id: item.category.id,
            name: item.category.name,
            slug: item.category.slug,
          }
        : null,
      viewCount: item.viewCount,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    };
  }
}
