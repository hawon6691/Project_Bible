import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CartItem } from './entities/cart-item.entity';
import { AddCartItemDto, UpdateCartQuantityDto } from './dto/cart.dto';
import { BusinessException } from '../common/exceptions/business.exception';

@Injectable()
export class CartService {
  constructor(
    @InjectRepository(CartItem)
    private cartRepository: Repository<CartItem>,
  ) {}

  // ─── CART-01: 장바구니 조회 ───
  async getCart(userId: number) {
    const items = await this.cartRepository.find({
      where: { userId },
      relations: ['product', 'seller'],
      order: { createdAt: 'DESC' },
    });

    return items.map((item) => ({
      id: item.id,
      product: {
        id: item.product.id,
        name: item.product.name,
        thumbnailUrl: item.product.thumbnailUrl,
        price: item.product.price,
        lowestPrice: item.product.lowestPrice,
      },
      seller: {
        id: item.seller.id,
        name: item.seller.name,
        logoUrl: item.seller.logoUrl,
      },
      selectedOptions: item.selectedOptions,
      quantity: item.quantity,
      createdAt: item.createdAt,
    }));
  }

  // ─── CART-02: 장바구니 추가 ───
  async addItem(userId: number, dto: AddCartItemDto) {
    // 동일 상품+판매처+옵션이면 수량 증가
    const existing = await this.cartRepository.findOne({
      where: {
        userId,
        productId: dto.productId,
        sellerId: dto.sellerId,
        selectedOptions: dto.selectedOptions || null,
      },
    });

    if (existing) {
      existing.quantity += dto.quantity;
      const saved = await this.cartRepository.save(existing);
      return { id: saved.id, quantity: saved.quantity };
    }

    const item = this.cartRepository.create({
      userId,
      productId: dto.productId,
      sellerId: dto.sellerId,
      quantity: dto.quantity,
      selectedOptions: dto.selectedOptions || null,
    });

    const saved = await this.cartRepository.save(item);
    return { id: saved.id, quantity: saved.quantity };
  }

  // ─── CART-03: 수량 변경 ───
  async updateQuantity(userId: number, itemId: number, dto: UpdateCartQuantityDto) {
    const item = await this.cartRepository.findOne({
      where: { id: itemId, userId },
    });
    if (!item) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    item.quantity = dto.quantity;
    const saved = await this.cartRepository.save(item);
    return { id: saved.id, quantity: saved.quantity };
  }

  // ─── CART-04: 항목 삭제 ───
  async removeItem(userId: number, itemId: number) {
    const item = await this.cartRepository.findOne({
      where: { id: itemId, userId },
    });
    if (!item) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.cartRepository.remove(item);
    return { message: '장바구니에서 삭제되었습니다.' };
  }

  // ─── CART-05: 전체 비우기 ───
  async clearCart(userId: number) {
    await this.cartRepository.delete({ userId });
    return { message: '장바구니가 비워졌습니다.' };
  }
}
