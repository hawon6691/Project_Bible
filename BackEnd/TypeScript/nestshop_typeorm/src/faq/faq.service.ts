import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Brackets, Repository } from 'typeorm';
import { PaginationRequestDto, PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { CreateFaqDto } from './dto/create-faq.dto';
import { CreateNoticeDto } from './dto/create-notice.dto';
import { FaqQueryDto } from './dto/faq-query.dto';
import { UpdateFaqDto } from './dto/update-faq.dto';
import { UpdateNoticeDto } from './dto/update-notice.dto';
import { Faq } from './entities/faq.entity';
import { Notice } from './entities/notice.entity';

@Injectable()
export class FaqService {
  constructor(
    @InjectRepository(Faq)
    private faqRepository: Repository<Faq>,
    @InjectRepository(Notice)
    private noticeRepository: Repository<Notice>,
  ) {}

  // FAQ-01/02: FAQ 목록 조회 + 검색
  async findFaqs(query: FaqQueryDto) {
    const qb = this.faqRepository
      .createQueryBuilder('faq')
      .where('faq.isActive = :isActive', { isActive: true });

    if (query.category) {
      qb.andWhere('faq.category = :category', { category: query.category });
    }

    if (query.keyword) {
      qb.andWhere(
        new Brackets((subQb) => {
          subQb
            .where('faq.question LIKE :keyword', { keyword: `%${query.keyword}%` })
            .orWhere('faq.answer LIKE :keyword', { keyword: `%${query.keyword}%` });
        }),
      );
    }

    qb.orderBy('faq.createdAt', 'DESC').skip(query.skip).take(query.limit);

    const [items, totalItems] = await qb.getManyAndCount();
    const mapped = items.map((faq) => this.toFaqDetail(faq));
    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  // FAQ-03: FAQ 등록 (Admin)
  async createFaq(dto: CreateFaqDto) {
    const faq = this.faqRepository.create({
      category: dto.category,
      question: dto.question,
      answer: dto.answer,
      isActive: dto.isActive ?? true,
    });
    const saved = await this.faqRepository.save(faq);
    return this.toFaqDetail(saved);
  }

  // FAQ-04: FAQ 수정 (Admin)
  async updateFaq(id: number, dto: UpdateFaqDto) {
    const faq = await this.faqRepository.findOne({ where: { id } });
    if (!faq) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.category !== undefined) faq.category = dto.category;
    if (dto.question !== undefined) faq.question = dto.question;
    if (dto.answer !== undefined) faq.answer = dto.answer;
    if (dto.isActive !== undefined) faq.isActive = dto.isActive;

    const saved = await this.faqRepository.save(faq);
    return this.toFaqDetail(saved);
  }

  // FAQ-04: FAQ 삭제 (Admin) - 소프트 삭제
  async removeFaq(id: number) {
    const faq = await this.faqRepository.findOne({ where: { id } });
    if (!faq) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.faqRepository.softRemove(faq);
    return { message: 'FAQ가 삭제되었습니다.' };
  }

  // FAQ-05: 공지 목록
  async findNotices(query: PaginationRequestDto) {
    const [items, totalItems] = await this.noticeRepository.findAndCount({
      where: { isPublished: true },
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const mapped = items.map((notice) => this.toNoticeDetail(notice));
    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  // FAQ-06: 공지 등록 (Admin)
  async createNotice(dto: CreateNoticeDto) {
    const notice = this.noticeRepository.create({
      title: dto.title,
      content: dto.content,
      isPublished: dto.isPublished ?? true,
    });
    const saved = await this.noticeRepository.save(notice);
    return this.toNoticeDetail(saved);
  }

  // FAQ-06: 공지 수정 (Admin)
  async updateNotice(id: number, dto: UpdateNoticeDto) {
    const notice = await this.noticeRepository.findOne({ where: { id } });
    if (!notice) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.title !== undefined) notice.title = dto.title;
    if (dto.content !== undefined) notice.content = dto.content;
    if (dto.isPublished !== undefined) notice.isPublished = dto.isPublished;

    const saved = await this.noticeRepository.save(notice);
    return this.toNoticeDetail(saved);
  }

  // FAQ-06: 공지 삭제 (Admin) - 소프트 삭제
  async removeNotice(id: number) {
    const notice = await this.noticeRepository.findOne({ where: { id } });
    if (!notice) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.noticeRepository.softRemove(notice);
    return { message: '공지사항이 삭제되었습니다.' };
  }

  private toFaqDetail(faq: Faq) {
    return {
      id: faq.id,
      category: faq.category,
      question: faq.question,
      answer: faq.answer,
      isActive: faq.isActive,
      createdAt: faq.createdAt,
      updatedAt: faq.updatedAt,
    };
  }

  private toNoticeDetail(notice: Notice) {
    return {
      id: notice.id,
      title: notice.title,
      content: notice.content,
      isPublished: notice.isPublished,
      createdAt: notice.createdAt,
      updatedAt: notice.updatedAt,
    };
  }
}
