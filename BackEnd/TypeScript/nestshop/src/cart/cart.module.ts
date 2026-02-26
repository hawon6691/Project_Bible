import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CartController } from './cart.controller';
import { CartService } from './cart.service';
import { CartItem } from './entities/cart-item.entity';
import { Product } from '../product/entities/product.entity';
import { Seller } from '../seller/entities/seller.entity';

@Module({
  imports: [TypeOrmModule.forFeature([CartItem, Product, Seller])],
  controllers: [CartController],
  providers: [CartService],
  exports: [CartService],
})
export class CartModule {}
