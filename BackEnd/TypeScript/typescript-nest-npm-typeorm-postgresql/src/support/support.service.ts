import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { User } from '../user/entities/user.entity';
import { AnswerSupportTicketDto } from './dto/answer-support-ticket.dto';
import { CreateSupportTicketDto } from './dto/create-support-ticket.dto';
import { SupportTicketQueryDto } from './dto/support-ticket-query.dto';
import { SupportTicket, SupportTicketStatus } from './entities/support-ticket.entity';

@Injectable()
export class SupportService {
  constructor(
    @InjectRepository(SupportTicket)
    private supportTicketRepository: Repository<SupportTicket>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  // SUP-01: 1:1 문의 작성
  async create(userId: number, dto: CreateSupportTicketDto) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const ticket = this.supportTicketRepository.create({
      userId,
      category: dto.category,
      title: dto.title,
      content: dto.content,
      attachmentUrl: dto.attachmentUrl ?? null,
      status: SupportTicketStatus.OPEN,
    });
    const saved = await this.supportTicketRepository.save(ticket);

    return this.findMyOne(userId, saved.id);
  }

  // SUP-02: 내 문의 목록
  async findMine(userId: number, query: SupportTicketQueryDto) {
    const where = query.status ? { userId, status: query.status } : { userId };
    const [items, totalItems] = await this.supportTicketRepository.findAndCount({
      where,
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const mapped = items.map((item) => this.toDetail(item));
    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  // SUP-03: 내 문의 상세
  async findMyOne(userId: number, ticketId: number) {
    const ticket = await this.supportTicketRepository.findOne({
      where: { id: ticketId, userId },
    });
    if (!ticket) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return this.toDetail(ticket);
  }

  // SUP-04: 1:1 문의 답변 (Admin)
  async answer(adminUserId: number, ticketId: number, dto: AnswerSupportTicketDto) {
    const ticket = await this.supportTicketRepository.findOne({ where: { id: ticketId } });
    if (!ticket) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    ticket.answerContent = dto.content;
    ticket.answeredBy = adminUserId;
    ticket.answeredAt = new Date();
    ticket.status = SupportTicketStatus.ANSWERED;

    const saved = await this.supportTicketRepository.save(ticket);
    return this.toDetail(saved);
  }

  // SUP-05: 전체 문의 관리 (Admin)
  async findAll(query: SupportTicketQueryDto) {
    const qb = this.supportTicketRepository
      .createQueryBuilder('ticket')
      .leftJoinAndSelect('ticket.user', 'user');

    if (query.status) {
      qb.andWhere('ticket.status = :status', { status: query.status });
    }

    qb.orderBy('ticket.createdAt', 'DESC').skip(query.skip).take(query.limit);

    const [items, totalItems] = await qb.getManyAndCount();
    const mapped = items.map((item) => ({
      ...this.toDetail(item),
      user: {
        id: item.user?.id,
        email: item.user?.email,
        nickname: item.user?.nickname,
      },
    }));

    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  private toDetail(ticket: SupportTicket) {
    return {
      id: ticket.id,
      category: ticket.category,
      title: ticket.title,
      content: ticket.content,
      attachmentUrl: ticket.attachmentUrl,
      status: ticket.status,
      answer: ticket.answerContent
        ? {
            content: ticket.answerContent,
            answeredBy: ticket.answeredBy,
            answeredAt: ticket.answeredAt,
          }
        : null,
      createdAt: ticket.createdAt,
      updatedAt: ticket.updatedAt,
    };
  }
}
