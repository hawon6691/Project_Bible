import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Seller } from './entities/seller.entity';
import { CreateSellerDto } from './dto/create-seller.dto';
import { UpdateSellerDto } from './dto/update-seller.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';

@Injectable()
export class SellerService {
  constructor(
    @InjectRepository(Seller)
    private sellerRepository: Repository<Seller>,
  ) {}

  // ─── SELL-01: 판매처 목록 조회 ───
  async findAll(query: PaginationRequestDto) {
    const [items, totalItems] = await this.sellerRepository.findAndCount({
      where: { isActive: true },
      skip: query.skip,
      take: query.limit,
      order: { trustScore: 'DESC', name: 'ASC' },
    });

    return new PaginationResponseDto(
      items.map((s) => this.toResponse(s)),
      totalItems,
      query.page,
      query.limit,
    );
  }

  // ─── SELL-01: 판매처 상세 ───
  async findOne(id: number) {
    const seller = await this.sellerRepository.findOne({ where: { id } });
    if (!seller) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return this.toResponse(seller);
  }

  // ─── SELL-02: 판매처 등록 ───
  async create(dto: CreateSellerDto) {
    const seller = this.sellerRepository.create({
      name: dto.name,
      url: dto.url,
      logoUrl: dto.logoUrl || null,
      description: dto.description || null,
    });
    const saved = await this.sellerRepository.save(seller);
    return this.toResponse(saved);
  }

  // ─── SELL-03: 판매처 수정 ───
  async update(id: number, dto: UpdateSellerDto) {
    const seller = await this.sellerRepository.findOne({ where: { id } });
    if (!seller) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.name !== undefined) seller.name = dto.name;
    if (dto.url !== undefined) seller.url = dto.url;
    if (dto.logoUrl !== undefined) seller.logoUrl = dto.logoUrl;
    if (dto.description !== undefined) seller.description = dto.description;
    if (dto.isActive !== undefined) seller.isActive = dto.isActive;

    const saved = await this.sellerRepository.save(seller);
    return this.toResponse(saved);
  }

  // ─── SELL-03: 판매처 삭제 ───
  async remove(id: number) {
    const seller = await this.sellerRepository.findOne({ where: { id } });
    if (!seller) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    await this.sellerRepository.remove(seller);
    return { message: '판매처가 삭제되었습니다.' };
  }

  private toResponse(seller: Seller) {
    return {
      id: seller.id,
      name: seller.name,
      url: seller.url,
      logoUrl: seller.logoUrl,
      trustScore: seller.trustScore,
      trustGrade: seller.trustGrade,
      description: seller.description,
      isActive: seller.isActive,
      createdAt: seller.createdAt,
    };
  }
}
