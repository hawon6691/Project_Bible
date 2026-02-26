import { ForbiddenException, HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { AuctionListQueryDto } from './dto/auction-list-query.dto';
import { CreateAuctionBidDto } from './dto/create-auction-bid.dto';
import { CreateAuctionDto } from './dto/create-auction.dto';
import { UpdateAuctionBidDto } from './dto/update-auction-bid.dto';
import { AuctionBid } from './entities/auction-bid.entity';
import { Auction, AuctionStatus } from './entities/auction.entity';

@Injectable()
export class AuctionService {
  constructor(
    @InjectRepository(Auction)
    private auctionRepository: Repository<Auction>,
    @InjectRepository(AuctionBid)
    private auctionBidRepository: Repository<AuctionBid>,
  ) {}

  async createAuction(ownerId: number, dto: CreateAuctionDto) {
    const created = this.auctionRepository.create({
      ownerId,
      title: dto.title,
      description: dto.description,
      categoryId: dto.categoryId,
      specs: dto.specs ?? null,
      budget: dto.budget,
      status: AuctionStatus.OPEN,
      selectedBidId: null,
    });

    const saved = await this.auctionRepository.save(created);
    return this.getAuctionDetail(saved.id);
  }

  async getAuctions(query: AuctionListQueryDto) {
    const qb = this.auctionRepository
      .createQueryBuilder('auction')
      .orderBy('auction.createdAt', 'DESC')
      .skip(query.skip)
      .take(query.limit);

    if (query.status) {
      qb.andWhere('auction.status = :status', { status: query.status.toUpperCase() });
    }

    if (query.categoryId) {
      qb.andWhere('auction.categoryId = :categoryId', { categoryId: Number(query.categoryId) });
    }

    const [items, total] = await qb.getManyAndCount();
    return new PaginationResponseDto(items.map((a) => this.toAuctionSummary(a)), total, query.page, query.limit);
  }

  async getAuctionDetail(auctionId: number) {
    const auction = await this.ensureAuction(auctionId);
    const bids = await this.auctionBidRepository.find({
      where: { auctionId },
      order: { price: 'ASC', createdAt: 'ASC' },
    });

    return {
      ...this.toAuctionDetail(auction),
      bids: bids.map((bid) => this.toBidDetail(bid)),
    };
  }

  async createBid(userId: number, auctionId: number, dto: CreateAuctionBidDto) {
    const auction = await this.ensureAuction(auctionId);
    this.ensureAuctionOpen(auction);

    const created = this.auctionBidRepository.create({
      auctionId,
      sellerId: userId,
      price: dto.price,
      description: dto.description ?? null,
      deliveryDays: dto.deliveryDays,
    });

    const saved = await this.auctionBidRepository.save(created);
    return this.toBidDetail(saved);
  }

  async selectBid(ownerId: number, auctionId: number, bidId: number) {
    const auction = await this.ensureAuction(auctionId);

    if (auction.ownerId !== ownerId) {
      throw new ForbiddenException('본인 경매에서만 낙찰을 선택할 수 있습니다.');
    }

    this.ensureAuctionOpen(auction);

    const bid = await this.auctionBidRepository.findOne({ where: { id: bidId, auctionId } });
    if (!bid) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    auction.selectedBidId = bid.id;
    auction.status = AuctionStatus.CLOSED;
    await this.auctionRepository.save(auction);

    return { success: true, message: '낙찰이 선택되었습니다.' };
  }

  async cancelAuction(ownerId: number, auctionId: number) {
    const auction = await this.ensureAuction(auctionId);

    if (auction.ownerId !== ownerId) {
      throw new ForbiddenException('본인 경매만 취소할 수 있습니다.');
    }

    if (auction.status !== AuctionStatus.OPEN) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '진행 중인 경매만 취소할 수 있습니다.');
    }

    auction.status = AuctionStatus.CANCELLED;
    await this.auctionRepository.save(auction);

    return { success: true, message: '경매가 취소되었습니다.' };
  }

  async updateBid(userId: number, auctionId: number, bidId: number, dto: UpdateAuctionBidDto) {
    const auction = await this.ensureAuction(auctionId);
    this.ensureAuctionOpen(auction);

    const bid = await this.auctionBidRepository.findOne({ where: { id: bidId, auctionId } });
    if (!bid) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (bid.sellerId !== userId) {
      throw new ForbiddenException('본인 입찰만 수정할 수 있습니다.');
    }

    if (dto.price !== undefined) bid.price = dto.price;
    if (dto.description !== undefined) bid.description = dto.description;
    if (dto.deliveryDays !== undefined) bid.deliveryDays = dto.deliveryDays;

    const saved = await this.auctionBidRepository.save(bid);
    return this.toBidDetail(saved);
  }

  async removeBid(userId: number, auctionId: number, bidId: number) {
    const auction = await this.ensureAuction(auctionId);
    this.ensureAuctionOpen(auction);

    const bid = await this.auctionBidRepository.findOne({ where: { id: bidId, auctionId } });
    if (!bid) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (bid.sellerId !== userId) {
      throw new ForbiddenException('본인 입찰만 취소할 수 있습니다.');
    }

    await this.auctionBidRepository.softDelete({ id: bidId });
    return { success: true, message: '입찰이 취소되었습니다.' };
  }

  private async ensureAuction(auctionId: number) {
    const auction = await this.auctionRepository.findOne({ where: { id: auctionId } });
    if (!auction) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return auction;
  }

  private ensureAuctionOpen(auction: Auction) {
    if (auction.status !== AuctionStatus.OPEN) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '진행 중인 경매가 아닙니다.');
    }
  }

  private toAuctionSummary(auction: Auction) {
    return {
      id: auction.id,
      ownerId: auction.ownerId,
      title: auction.title,
      categoryId: auction.categoryId,
      budget: auction.budget,
      status: auction.status,
      createdAt: auction.createdAt,
    };
  }

  private toAuctionDetail(auction: Auction) {
    return {
      id: auction.id,
      ownerId: auction.ownerId,
      title: auction.title,
      description: auction.description,
      categoryId: auction.categoryId,
      specs: auction.specs,
      budget: auction.budget,
      status: auction.status,
      selectedBidId: auction.selectedBidId,
      createdAt: auction.createdAt,
      updatedAt: auction.updatedAt,
    };
  }

  private toBidDetail(bid: AuctionBid) {
    return {
      id: bid.id,
      auctionId: bid.auctionId,
      sellerId: bid.sellerId,
      price: bid.price,
      description: bid.description,
      deliveryDays: bid.deliveryDays,
      createdAt: bid.createdAt,
      updatedAt: bid.updatedAt,
    };
  }
}
