import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { UserRole } from '../common/decorators/roles.decorator';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { Product } from '../product/entities/product.entity';
import { InquiryQueryDto } from './dto/inquiry-query.dto';
import { CreateInquiryDto } from './dto/create-inquiry.dto';
import { AnswerInquiryDto } from './dto/answer-inquiry.dto';
import { Inquiry, InquiryStatus } from './entities/inquiry.entity';

@Injectable()
export class InquiryService {
  constructor(
    @InjectRepository(Inquiry)
    private inquiryRepository: Repository<Inquiry>,
    @InjectRepository(Product)
    private productRepository: Repository<Product>,
  ) {}

  // INQ-01: 상품 문의 작성
  async create(userId: number, productId: number, dto: CreateInquiryDto) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const inquiry = this.inquiryRepository.create({
      userId,
      productId,
      title: dto.title,
      content: dto.content,
      isSecret: dto.isSecret ?? false,
      status: InquiryStatus.PENDING,
    });
    const saved = await this.inquiryRepository.save(inquiry);
    return this.findOne(saved.id, userId, UserRole.USER);
  }

  // INQ-02: 상품 문의 목록
  async findByProduct(productId: number, query: InquiryQueryDto, viewerId?: number, viewerRole?: UserRole) {
    const product = await this.productRepository.findOne({ where: { id: productId } });
    if (!product) {
      throw new BusinessException('PRODUCT_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const qb = this.inquiryRepository
      .createQueryBuilder('inquiry')
      .leftJoinAndSelect('inquiry.user', 'user')
      .where('inquiry.productId = :productId', { productId });

    if (query.status) {
      qb.andWhere('inquiry.status = :status', { status: query.status });
    }

    qb.orderBy('inquiry.createdAt', 'DESC').skip(query.skip).take(query.limit);

    const [items, totalItems] = await qb.getManyAndCount();
    const mapped = items.map((item) => this.toDetail(item, viewerId, viewerRole));
    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  // INQ-03: 문의 답변
  async answer(inquiryId: number, actorId: number, role: UserRole, dto: AnswerInquiryDto) {
    if (![UserRole.ADMIN, UserRole.SELLER].includes(role)) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }

    const inquiry = await this.inquiryRepository.findOne({ where: { id: inquiryId } });
    if (!inquiry) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    inquiry.answerContent = dto.content;
    inquiry.answeredBy = actorId;
    inquiry.answeredAt = new Date();
    inquiry.status = InquiryStatus.ANSWERED;

    const saved = await this.inquiryRepository.save(inquiry);
    return this.findOne(saved.id, actorId, role);
  }

  // INQ-04: 내 문의 목록
  async findMine(userId: number, query: InquiryQueryDto) {
    const [items, totalItems] = await this.inquiryRepository.findAndCount({
      where: query.status ? { userId, status: query.status } : { userId },
      relations: ['product'],
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const mapped = items.map((item) => ({
      id: item.id,
      product: {
        id: item.product.id,
        name: item.product.name,
      },
      title: item.title,
      content: item.content,
      isSecret: item.isSecret,
      status: item.status,
      answer: item.answerContent
        ? {
            content: item.answerContent,
            answeredBy: item.answeredBy,
            answeredAt: item.answeredAt,
          }
        : null,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    }));

    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  // INQ-05: 문의 삭제 (답변 전 작성자만)
  async remove(userId: number, inquiryId: number) {
    const inquiry = await this.inquiryRepository.findOne({ where: { id: inquiryId } });
    if (!inquiry) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    if (inquiry.userId !== userId) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }
    if (inquiry.status === InquiryStatus.ANSWERED) {
      throw new BusinessException(
        'VALIDATION_FAILED',
        HttpStatus.BAD_REQUEST,
        '답변 완료된 문의는 삭제할 수 없습니다.',
      );
    }

    await this.inquiryRepository.softRemove(inquiry);
    return { message: '문의가 삭제되었습니다.' };
  }

  async findOne(inquiryId: number, viewerId?: number, viewerRole?: UserRole) {
    const inquiry = await this.inquiryRepository.findOne({
      where: { id: inquiryId },
      relations: ['user', 'product'],
    });
    if (!inquiry) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return this.toDetail(inquiry, viewerId, viewerRole);
  }

  private toDetail(inquiry: Inquiry, viewerId?: number, viewerRole?: UserRole) {
    // 비밀글은 작성자/관리자/판매자만 원문을 볼 수 있다.
    const canViewSecret = !inquiry.isSecret
      || inquiry.userId === viewerId
      || viewerRole === UserRole.ADMIN
      || viewerRole === UserRole.SELLER;

    return {
      id: inquiry.id,
      product: {
        id: inquiry.product?.id ?? inquiry.productId,
        name: inquiry.product?.name,
      },
      author: {
        id: inquiry.user?.id ?? inquiry.userId,
        nickname: inquiry.user?.nickname,
      },
      title: canViewSecret ? inquiry.title : '비밀 문의입니다.',
      content: canViewSecret ? inquiry.content : '비밀 문의 내용은 작성자와 관리자만 확인할 수 있습니다.',
      isSecret: inquiry.isSecret,
      status: inquiry.status,
      answer: inquiry.answerContent
        ? {
            content: canViewSecret ? inquiry.answerContent : '비밀 문의 답변입니다.',
            answeredBy: inquiry.answeredBy,
            answeredAt: inquiry.answeredAt,
          }
        : null,
      createdAt: inquiry.createdAt,
      updatedAt: inquiry.updatedAt,
    };
  }
}
