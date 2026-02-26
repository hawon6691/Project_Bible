import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Brackets, Repository } from 'typeorm';
import { UserRole } from '../common/decorators/roles.decorator';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { User } from '../user/entities/user.entity';
import { CommunityPostQueryDto, CommunityPostSort } from './dto/community-post-query.dto';
import { CreateCommunityPostDto } from './dto/create-community-post.dto';
import { UpdateCommunityPostDto } from './dto/update-community-post.dto';
import { CommunityBoardType, CommunityPost } from './entities/community-post.entity';

@Injectable()
export class CommunityService {
  constructor(
    @InjectRepository(CommunityPost)
    private communityPostRepository: Repository<CommunityPost>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  // COMM-01: 게시판 목록 조회
  getBoards() {
    return [
      { key: CommunityBoardType.REVIEW, name: '사용기' },
      { key: CommunityBoardType.QNA, name: 'Q&A' },
      { key: CommunityBoardType.FREE, name: '자유게시판' },
    ];
  }

  // COMM-02: 게시글 목록 조회
  async findPosts(query: CommunityPostQueryDto) {
    const qb = this.communityPostRepository
      .createQueryBuilder('post')
      .leftJoinAndSelect('post.user', 'user');

    if (query.boardType) {
      qb.andWhere('post.boardType = :boardType', { boardType: query.boardType });
    }

    if (query.keyword) {
      qb.andWhere(
        new Brackets((subQb) => {
          subQb
            .where('post.title LIKE :keyword', { keyword: `%${query.keyword}%` })
            .orWhere('post.content LIKE :keyword', { keyword: `%${query.keyword}%` });
        }),
      );
    }

    // 정렬 옵션은 최신순/조회순만 우선 제공한다.
    if (query.sort === CommunityPostSort.VIEWS) {
      qb.orderBy('post.viewCount', 'DESC').addOrderBy('post.createdAt', 'DESC');
    } else {
      qb.orderBy('post.createdAt', 'DESC');
    }

    qb.skip(query.skip).take(query.limit);

    const [items, totalItems] = await qb.getManyAndCount();
    const mapped = items.map((post) => this.toSummary(post));

    return new PaginationResponseDto(mapped, totalItems, query.page, query.limit);
  }

  // COMM-03: 게시글 상세 조회
  async findOne(postId: number) {
    const post = await this.communityPostRepository.findOne({
      where: { id: postId },
      relations: ['user'],
    });
    if (!post) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    post.viewCount += 1;
    await this.communityPostRepository.save(post);

    return this.toDetail(post);
  }

  // COMM-04: 게시글 작성
  async create(userId: number, dto: CreateCommunityPostDto) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    const post = this.communityPostRepository.create({
      userId,
      boardType: dto.boardType,
      title: dto.title,
      content: dto.content,
    });
    const saved = await this.communityPostRepository.save(post);
    const full = await this.communityPostRepository.findOne({
      where: { id: saved.id },
      relations: ['user'],
    });
    if (!full) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return this.toDetail(full);
  }

  // COMM-05: 게시글 수정
  async update(userId: number, role: UserRole, postId: number, dto: UpdateCommunityPostDto) {
    const post = await this.communityPostRepository.findOne({ where: { id: postId } });
    if (!post) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    // 작성자 또는 관리자만 수정할 수 있다.
    if (post.userId !== userId && role !== UserRole.ADMIN) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }

    if (dto.boardType !== undefined) post.boardType = dto.boardType;
    if (dto.title !== undefined) post.title = dto.title;
    if (dto.content !== undefined) post.content = dto.content;

    const saved = await this.communityPostRepository.save(post);
    const full = await this.communityPostRepository.findOne({
      where: { id: saved.id },
      relations: ['user'],
    });
    if (!full) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return this.toDetail(full);
  }

  // COMM-06: 게시글 삭제
  async remove(userId: number, role: UserRole, postId: number) {
    const post = await this.communityPostRepository.findOne({ where: { id: postId } });
    if (!post) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (post.userId !== userId && role !== UserRole.ADMIN) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }

    await this.communityPostRepository.softRemove(post);
    return { message: '게시글이 삭제되었습니다.' };
  }

  private toSummary(post: CommunityPost) {
    return {
      id: post.id,
      boardType: post.boardType,
      title: post.title,
      viewCount: post.viewCount,
      author: {
        id: post.user?.id,
        nickname: post.user?.nickname,
      },
      createdAt: post.createdAt,
      updatedAt: post.updatedAt,
    };
  }

  private toDetail(post: CommunityPost) {
    return {
      ...this.toSummary(post),
      content: post.content,
    };
  }
}
