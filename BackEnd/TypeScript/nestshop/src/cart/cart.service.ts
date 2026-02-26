import { HttpStatus, Injectable, OnModuleDestroy } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { InjectRepository } from '@nestjs/typeorm';
import Redis from 'ioredis';
import { randomUUID } from 'crypto';
import { In, Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { Product } from '../product/entities/product.entity';
import { Seller } from '../seller/entities/seller.entity';
import { AddCartItemDto, UpdateCartQuantityDto } from './dto/cart.dto';
import { CartItem } from './entities/cart-item.entity';

interface GuestCartItem {
  id: string;
  productId: number;
  sellerId: number;
  selectedOptions: string | null;
  quantity: number;
  createdAt: string;
}

@Injectable()
export class CartService implements OnModuleDestroy {
  private readonly redis: Redis;
  private readonly guestCartTtlSeconds = 60 * 60 * 24;

  constructor(
    @InjectRepository(CartItem)
    private readonly cartRepository: Repository<CartItem>,
    @InjectRepository(Product)
    private readonly productRepository: Repository<Product>,
    @InjectRepository(Seller)
    private readonly sellerRepository: Repository<Seller>,
    private readonly configService: ConfigService,
  ) {
    this.redis = new Redis({
      host: this.configService.get<string>('REDIS_HOST', 'localhost'),
      port: this.configService.get<number>('REDIS_PORT', 6379),
      password: this.configService.get<string>('REDIS_PASSWORD') || undefined,
      lazyConnect: true,
      maxRetriesPerRequest: 1,
    });
  }

  async onModuleDestroy() {
    if (this.redis.status !== 'end') {
      await this.redis.quit();
    }
  }

  // ─── CART-01: 회원 장바구니 조회 ───
  async getCart(userId: number) {
    const items = await this.cartRepository.find({
      where: { userId },
      relations: ['product', 'seller'],
      order: { createdAt: 'DESC' },
    });

    return items.map((item) => this.toUserCartResponse(item));
  }

  // ─── CART-02: 회원 장바구니 추가 ───
  async addItem(userId: number, dto: AddCartItemDto) {
    const existing = await this.cartRepository.findOne({
      where: {
        userId,
        productId: dto.productId,
        sellerId: dto.sellerId,
        selectedOptions: (dto.selectedOptions || undefined) as string | undefined,
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

  // ─── CART-03: 회원 장바구니 수량 변경 ───
  async updateQuantity(userId: number, itemId: number, dto: UpdateCartQuantityDto) {
    const item = await this.cartRepository.findOne({ where: { id: itemId, userId } });
    if (!item) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    item.quantity = dto.quantity;
    const saved = await this.cartRepository.save(item);
    return { id: saved.id, quantity: saved.quantity };
  }

  // ─── CART-04: 회원 장바구니 항목 삭제 ───
  async removeItem(userId: number, itemId: number) {
    const item = await this.cartRepository.findOne({ where: { id: itemId, userId } });
    if (!item) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.cartRepository.remove(item);
    return { message: '장바구니에서 삭제되었습니다.' };
  }

  // ─── CART-05: 회원 장바구니 비우기 ───
  async clearCart(userId: number) {
    await this.cartRepository.delete({ userId });
    return { message: '장바구니가 비워졌습니다.' };
  }

  // ─── ACT-06: 비회원 장바구니 조회 (Redis) ───
  async getGuestCart(guestCartKey: string) {
    const key = this.toGuestRedisKey(guestCartKey);
    const guestItems = await this.readGuestItems(key);
    return this.mapGuestItems(guestItems);
  }

  // ─── ACT-06: 비회원 장바구니 추가 (Redis) ───
  async addGuestItem(guestCartKey: string, dto: AddCartItemDto) {
    const key = this.toGuestRedisKey(guestCartKey);
    const items = await this.readGuestItems(key);

    const existing = items.find(
      (item) =>
        item.productId === dto.productId &&
        item.sellerId === dto.sellerId &&
        (item.selectedOptions ?? null) === (dto.selectedOptions ?? null),
    );

    if (existing) {
      existing.quantity += dto.quantity;
    } else {
      items.push({
        id: randomUUID(),
        productId: dto.productId,
        sellerId: dto.sellerId,
        selectedOptions: dto.selectedOptions ?? null,
        quantity: dto.quantity,
        createdAt: new Date().toISOString(),
      });
    }

    await this.writeGuestItems(key, items);
    return this.mapGuestItems(items);
  }

  // ─── ACT-06: 비회원 장바구니 수량 변경 (Redis) ───
  async updateGuestQuantity(guestCartKey: string, itemId: string, dto: UpdateCartQuantityDto) {
    const key = this.toGuestRedisKey(guestCartKey);
    const items = await this.readGuestItems(key);
    const item = items.find((entry) => entry.id === itemId);
    if (!item) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    item.quantity = dto.quantity;
    await this.writeGuestItems(key, items);
    return this.mapGuestItems(items);
  }

  // ─── ACT-06: 비회원 장바구니 항목 삭제 (Redis) ───
  async removeGuestItem(guestCartKey: string, itemId: string) {
    const key = this.toGuestRedisKey(guestCartKey);
    const items = await this.readGuestItems(key);
    const filtered = items.filter((entry) => entry.id !== itemId);
    if (filtered.length === items.length) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.writeGuestItems(key, filtered);
    return this.mapGuestItems(filtered);
  }

  // ─── ACT-06: 비회원 장바구니 전체 비우기 (Redis) ───
  async clearGuestCart(guestCartKey: string) {
    const key = this.toGuestRedisKey(guestCartKey);
    await this.ensureRedisConnection();
    await this.redis.del(key);
    return { message: '비회원 장바구니가 비워졌습니다.' };
  }

  // ─── ACT-06: 로그인 시 비회원 장바구니 병합 ───
  async mergeGuestCartToUser(userId: number, guestCartKey: string) {
    const key = this.toGuestRedisKey(guestCartKey);
    const guestItems = await this.readGuestItems(key);
    if (!guestItems.length) {
      return { mergedCount: 0, message: '병합할 비회원 장바구니가 없습니다.' };
    }

    for (const item of guestItems) {
      await this.addItem(userId, {
        productId: item.productId,
        sellerId: item.sellerId,
        quantity: item.quantity,
        selectedOptions: item.selectedOptions ?? undefined,
      });
    }

    await this.redis.del(key);
    return { mergedCount: guestItems.length, message: '비회원 장바구니가 병합되었습니다.' };
  }

  private async mapGuestItems(items: GuestCartItem[]) {
    if (!items.length) {
      return [];
    }

    const productIds = [...new Set(items.map((item) => item.productId))];
    const sellerIds = [...new Set(items.map((item) => item.sellerId))];

    const [products, sellers] = await Promise.all([
      this.productRepository.find({ where: { id: In(productIds) } }),
      this.sellerRepository.find({ where: { id: In(sellerIds) } }),
    ]);

    const productMap = new Map(products.map((item) => [item.id, item]));
    const sellerMap = new Map(sellers.map((item) => [item.id, item]));

    return items.map((item) => ({
      id: item.id,
      product: productMap.get(item.productId)
        ? {
            id: item.productId,
            name: productMap.get(item.productId)!.name,
            thumbnailUrl: productMap.get(item.productId)!.thumbnailUrl,
            price: productMap.get(item.productId)!.price,
            lowestPrice: productMap.get(item.productId)!.lowestPrice,
          }
        : null,
      seller: sellerMap.get(item.sellerId)
        ? {
            id: item.sellerId,
            name: sellerMap.get(item.sellerId)!.name,
            logoUrl: sellerMap.get(item.sellerId)!.logoUrl,
          }
        : null,
      selectedOptions: item.selectedOptions,
      quantity: item.quantity,
      createdAt: item.createdAt,
    }));
  }

  private toUserCartResponse(item: CartItem) {
    return {
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
    };
  }

  private toGuestRedisKey(guestCartKey: string) {
    const normalized = guestCartKey?.trim();
    if (!normalized) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '비회원 장바구니 키가 필요합니다.');
    }
    return `guest:cart:${normalized}`;
  }

  private async readGuestItems(redisKey: string): Promise<GuestCartItem[]> {
    await this.ensureRedisConnection();
    const raw = await this.redis.get(redisKey);
    if (!raw) return [];

    try {
      const parsed = JSON.parse(raw) as GuestCartItem[];
      if (!Array.isArray(parsed)) return [];
      return parsed;
    } catch {
      return [];
    }
  }

  private async writeGuestItems(redisKey: string, items: GuestCartItem[]) {
    await this.ensureRedisConnection();
    await this.redis.set(redisKey, JSON.stringify(items), 'EX', this.guestCartTtlSeconds);
  }

  private async ensureRedisConnection() {
    if (this.redis.status === 'wait') {
      await this.redis.connect();
    }
  }
}
