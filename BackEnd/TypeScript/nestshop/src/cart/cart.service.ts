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

interface UserCartCacheEntry {
  expiresAt: number;
  data: Array<{
    id: number;
    product: {
      id: number | null;
      name: string;
      thumbnailUrl: string | null;
      price: number;
      lowestPrice: number | null;
    } | null;
    seller: {
      id: number | null;
      name: string;
      logoUrl: string | null;
    } | null;
    selectedOptions: string | null;
    quantity: number;
    createdAt: string | Date;
  }>;
}

@Injectable()
export class CartService implements OnModuleDestroy {
  private readonly redis: Redis;
  private readonly guestCartTtlSeconds = 60 * 60 * 24;
  private readonly userCartCache = new Map<number, UserCartCacheEntry>();
  private readonly userCartCacheTtlMs = 1500;

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
      host: this.configService.get<string>('REDIS_HOST', '127.0.0.1'),
      port: this.configService.get<number>('REDIS_PORT', 6379),
      password: this.configService.get<string>('REDIS_PASSWORD') || undefined,
      lazyConnect: true,
      maxRetriesPerRequest: 1,
      connectTimeout: 1000,
      commandTimeout: 1500,
      enableOfflineQueue: false,
    });
  }

  async onModuleDestroy() {
    if (this.redis.status !== 'end') {
      await this.redis.quit();
    }
  }

  // ─── CART-01: 회원 장바구니 조회 ───
  async getCart(userId: number) {
    const now = Date.now();
    const cached = this.userCartCache.get(userId);
    if (cached && cached.expiresAt > now) {
      return cached.data;
    }

    // relations 기반 로딩 대신 필요한 컬럼만 단일 조인 조회해 응답 지연을 줄인다.
    const rows = await this.cartRepository
      .createQueryBuilder('cart')
      .leftJoin(Product, 'product', 'product.id = cart.productId')
      .leftJoin(Seller, 'seller', 'seller.id = cart.sellerId')
      .where('cart.userId = :userId', { userId })
      .orderBy('cart.createdAt', 'DESC')
      .select([
        'cart.id AS cart_id',
        'cart.selectedOptions AS cart_selected_options',
        'cart.quantity AS cart_quantity',
        'cart.createdAt AS cart_created_at',
        'product.id AS product_id',
        'product.name AS product_name',
        'product.thumbnailUrl AS product_thumbnail_url',
        'product.price AS product_price',
        'product.lowestPrice AS product_lowest_price',
        'seller.id AS seller_id',
        'seller.name AS seller_name',
        'seller.logoUrl AS seller_logo_url',
      ])
      .getRawMany<{
        cart_id: number;
        cart_selected_options: string | null;
        cart_quantity: number;
        cart_created_at: string | Date;
        product_id: number | null;
        product_name: string | null;
        product_thumbnail_url: string | null;
        product_price: number | null;
        product_lowest_price: number | null;
        seller_id: number | null;
        seller_name: string | null;
        seller_logo_url: string | null;
      }>();

    const data = rows.map((row) => ({
      id: Number(row.cart_id),
      product:
        row.product_id === null
          ? null
          : {
              id: Number(row.product_id),
              name: row.product_name ?? '',
              thumbnailUrl: row.product_thumbnail_url ?? null,
              price: Number(row.product_price ?? 0),
              lowestPrice:
                row.product_lowest_price === null
                  ? null
                  : Number(row.product_lowest_price),
            },
      seller:
        row.seller_id === null
          ? null
          : {
              id: Number(row.seller_id),
              name: row.seller_name ?? '',
              logoUrl: row.seller_logo_url ?? null,
            },
      selectedOptions: row.cart_selected_options ?? null,
      quantity: Number(row.cart_quantity),
      createdAt: row.cart_created_at,
    }));

    this.userCartCache.set(userId, {
      expiresAt: now + this.userCartCacheTtlMs,
      data,
    });
    return data;
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
      this.invalidateUserCartCache(userId);
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
    this.invalidateUserCartCache(userId);
    return { id: saved.id, quantity: saved.quantity };
  }

  // ─── CART-03: 회원 장바구니 수량 변경 ───
  async updateQuantity(userId: number, itemId: number, dto: UpdateCartQuantityDto) {
    const result = await this.cartRepository.update(
      { id: itemId, userId },
      { quantity: dto.quantity },
    );
    if (!result.affected) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    this.invalidateUserCartCache(userId);
    return { id: itemId, quantity: dto.quantity };
  }

  // ─── CART-04: 회원 장바구니 항목 삭제 ───
  async removeItem(userId: number, itemId: number) {
    const result = await this.cartRepository.delete({ id: itemId, userId });
    if (!result.affected) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    this.invalidateUserCartCache(userId);
    return { message: '장바구니에서 삭제되었습니다.' };
  }

  // ─── CART-05: 회원 장바구니 비우기 ───
  async clearCart(userId: number) {
    await this.cartRepository.delete({ userId });
    this.invalidateUserCartCache(userId);
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
    this.invalidateUserCartCache(userId);
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

  private invalidateUserCartCache(userId: number) {
    this.userCartCache.delete(userId);
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
