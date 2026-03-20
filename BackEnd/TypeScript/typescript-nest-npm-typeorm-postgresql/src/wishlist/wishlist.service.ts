import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Wishlist } from './entities/wishlist.entity';
import { Product } from '../product/entities/product.entity';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';

@Injectable()
export class WishlistService {
  constructor(
    @InjectRepository(Wishlist)
    private wishlistRepository: Repository<Wishlist>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
  ) {}

  // WISH-01: 내 위시리스트 조회
  async findMyWishlist(userId: number, query: PaginationRequestDto) {
    const [items, totalItems] = await this.wishlistRepository.findAndCount({
      where: { userId },
      relations: ['product'],
      skip: query.skip,
      take: query.limit,
      order: { createdAt: 'DESC' },
    });

    const mapped = items.map((item) => ({
      id: item.id,
      product: {
        id: item.product.id,
        name: item.product.name,
        thumbnailUrl: item.product.thumbnailUrl,
        lowestPrice: item.product.lowestPrice,
        averageRating: item.product.averageRating,
        reviewCount: item.product.reviewCount,
      },
      createdAt: item.createdAt,
    }));

    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  // WISH-02: 위시리스트 추가/토글
  async toggle(userId: number, productId: number) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const existing = await this.wishlistRepository.findOne({
      where: { userId, productId },
    });

    if (existing) {
      await this.wishlistRepository.remove(existing);
      return { wishlisted: false };
    }

    const created = this.wishlistRepository.create({ userId, productId });
    await this.wishlistRepository.save(created);
    return { wishlisted: true };
  }

  // WISH-03: 위시리스트 삭제
  async remove(userId: number, productId: number) {
    const existing = await this.wishlistRepository.findOne({
      where: { userId, productId },
    });
    if (!existing) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.wishlistRepository.remove(existing);
    return { message: '위시리스트에서 삭제되었습니다.' };
  }
}
