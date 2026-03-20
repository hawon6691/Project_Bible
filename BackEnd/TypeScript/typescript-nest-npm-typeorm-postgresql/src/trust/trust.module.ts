import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Seller } from '../seller/entities/seller.entity';
import { TrustController } from './trust.controller';
import { TrustService } from './trust.service';
import { TrustScoreHistory } from './entities/trust-score-history.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Seller, TrustScoreHistory])],
  controllers: [TrustController],
  providers: [TrustService],
  exports: [TrustService],
})
export class TrustModule {}
