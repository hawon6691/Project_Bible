import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PointController } from './point.controller';
import { PointService } from './point.service';
import { User } from '../user/entities/user.entity';
import { PointTransaction } from './entities/point-transaction.entity';

@Module({
  imports: [TypeOrmModule.forFeature([User, PointTransaction])],
  controllers: [PointController],
  providers: [PointService],
  exports: [PointService],
})
export class PointModule {}
