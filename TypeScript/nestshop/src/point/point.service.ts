import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { DataSource, Repository } from 'typeorm';
import { User } from '../user/entities/user.entity';
import { PointTransaction } from './entities/point-transaction.entity';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';
import { PointType } from '../common/constants/point-type.enum';
import { BusinessException } from '../common/exceptions/business.exception';
import { AdminGrantPointDto } from './dto/admin-grant-point.dto';

@Injectable()
export class PointService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
    @InjectRepository(PointTransaction)
    private pointTransactionRepository: Repository<PointTransaction>,
    private dataSource: DataSource,
  ) {}

  // PNT-01: 내 포인트 잔액 조회
  async getBalance(userId: number) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return {
      balance: user.point,
    };
  }

  // PNT-02: 내 포인트 내역 조회
  async getTransactions(userId: number, query: PaginationRequestDto) {
    const [items, totalItems] = await this.pointTransactionRepository.findAndCount({
      where: { userId },
      skip: query.skip,
      take: query.limit,
      order: { createdAt: 'DESC' },
    });

    const mapped = items.map((item) => ({
      id: item.id,
      type: item.type,
      amount: item.amount,
      balanceAfter: item.balanceAfter,
      description: item.description,
      orderId: item.orderId,
      createdAt: item.createdAt,
    }));

    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  // PNT-06: 관리자 수동 포인트 지급
  async adminGrant(adminUserId: number, dto: AdminGrantPointDto) {
    return this.dataSource.transaction(async (manager) => {
      const userRepo = manager.getRepository(User);
      const pointTransactionRepo = manager.getRepository(PointTransaction);

      const user = await userRepo.findOne({ where: { id: dto.userId } });
      if (!user) {
        throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      user.point += dto.amount;
      await userRepo.save(user);

      const transaction = pointTransactionRepo.create({
        userId: user.id,
        type: PointType.EARN,
        amount: dto.amount,
        balanceAfter: user.point,
        orderId: null,
        description: dto.description ?? `관리자(${adminUserId}) 수동 지급`,
      });
      await pointTransactionRepo.save(transaction);

      return {
        userId: user.id,
        grantedAmount: dto.amount,
        balance: user.point,
      };
    });
  }
}
