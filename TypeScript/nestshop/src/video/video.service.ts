import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectQueue } from '@nestjs/bull';
import { InjectRepository } from '@nestjs/typeorm';
import { Queue } from 'bull';
import { In, LessThan, Repository } from 'typeorm';
import { PaginationResponseDto } from '../common/dto/pagination.dto';
import { BusinessException } from '../common/exceptions/business.exception';
import { User } from '../user/entities/user.entity';
import { CreateShortformCommentDto } from './dto/create-shortform-comment.dto';
import { CreateShortformDto } from './dto/create-shortform.dto';
import { ShortformCommentQueryDto } from './dto/shortform-comment-query.dto';
import { ShortformFeedQueryDto } from './dto/shortform-feed-query.dto';
import { ShortformRankingPeriod, ShortformRankingQueryDto } from './dto/shortform-ranking-query.dto';
import { ShortformComment } from './entities/shortform-comment.entity';
import { ShortformLike } from './entities/shortform-like.entity';
import { ShortformProduct } from './entities/shortform-product.entity';
import { Shortform, ShortformTranscodeStatus } from './entities/shortform.entity';
import { VideoTranscodeJobData } from './video.processor';

@Injectable()
export class VideoService {
  private static readonly MAX_VIDEO_SIZE = 200 * 1024 * 1024;
  private static readonly ALLOWED_VIDEO_MIME = new Set(['video/mp4', 'video/webm', 'video/quicktime']);

  constructor(
    @InjectRepository(Shortform)
    private shortformRepository: Repository<Shortform>,
    @InjectRepository(ShortformLike)
    private shortformLikeRepository: Repository<ShortformLike>,
    @InjectRepository(ShortformComment)
    private shortformCommentRepository: Repository<ShortformComment>,
    @InjectRepository(ShortformProduct)
    private shortformProductRepository: Repository<ShortformProduct>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
    @InjectQueue('video-transcode')
    private readonly videoTranscodeQueue: Queue<VideoTranscodeJobData>,
  ) {}

  // 숏폼 업로드 메타데이터를 생성한다.
  async createShortform(userId: number, file: Express.Multer.File | undefined, dto: CreateShortformDto) {
    await this.ensureUser(userId);
    this.validateVideo(file, dto.durationSec);
    const video = file as Express.Multer.File;

    const storedFileName = video.filename ?? `${Date.now()}-${Math.random().toString(36).slice(2, 10)}.mp4`;
    const fileBaseName = storedFileName.replace(/\.[^/.]+$/, '');

    const shortform = this.shortformRepository.create({
      userId,
      title: dto.title,
      // 원본은 raw 경로에 저장하고, 트랜스코딩 완료 후 별도 URL을 채운다.
      videoUrl: `/uploads/shortforms/raw/${storedFileName}`,
      thumbnailUrl: `/uploads/shortforms/thumb/${fileBaseName}.jpg`,
      durationSec: dto.durationSec ?? 0,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      transcodeStatus: ShortformTranscodeStatus.PENDING,
      transcodedVideoUrl: null,
      transcodeError: null,
      transcodedAt: null,
    });

    const saved = await this.shortformRepository.save(shortform);

    if (dto.productIds?.length) {
      const products = dto.productIds.map((productId) =>
        this.shortformProductRepository.create({ shortformId: saved.id, productId }),
      );
      await this.shortformProductRepository.save(products);
    }

    // 업로드 직후 비디오 트랜스코딩 작업을 큐에 등록한다.
    await this.videoTranscodeQueue.add(
      'transcode',
      { shortformId: saved.id },
      {
        attempts: 3,
        backoff: { type: 'fixed', delay: 1000 },
        removeOnComplete: true,
      },
    );

    return this.getShortformDetail(saved.id, userId);
  }

  async getFeed(query: ShortformFeedQueryDto, viewerId?: number) {
    const [items, total] = await this.shortformRepository.findAndCount({
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const feed = await this.mapShortforms(items, viewerId);
    return new PaginationResponseDto(feed, total, query.page, query.limit);
  }

  async getShortformDetail(id: number, viewerId?: number) {
    const shortform = await this.shortformRepository.findOne({ where: { id } });
    if (!shortform) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    await this.shortformRepository.increment({ id }, 'viewCount', 1);

    const mapped = await this.mapShortforms([shortform], viewerId);
    return mapped[0];
  }

  async toggleLike(userId: number, shortformId: number) {
    await this.ensureUser(userId);
    await this.ensureShortform(shortformId);

    const existing = await this.shortformLikeRepository.findOne({ where: { userId, shortformId } });

    if (existing) {
      await this.shortformLikeRepository.softDelete({ id: existing.id });
      await this.shortformRepository.decrement({ id: shortformId }, 'likeCount', 1);
      const likeCount = await this.shortformLikeRepository.count({ where: { shortformId } });
      await this.shortformRepository.update(shortformId, { likeCount });
      return { liked: false, likeCount };
    }

    const like = this.shortformLikeRepository.create({ userId, shortformId });
    await this.shortformLikeRepository.save(like);
    await this.shortformRepository.increment({ id: shortformId }, 'likeCount', 1);
    const likeCount = await this.shortformLikeRepository.count({ where: { shortformId } });
    await this.shortformRepository.update(shortformId, { likeCount });
    return { liked: true, likeCount };
  }

  async createComment(userId: number, shortformId: number, dto: CreateShortformCommentDto) {
    await this.ensureUser(userId);
    await this.ensureShortform(shortformId);

    const comment = this.shortformCommentRepository.create({
      userId,
      shortformId,
      content: dto.content,
    });

    const saved = await this.shortformCommentRepository.save(comment);
    await this.shortformRepository.increment({ id: shortformId }, 'commentCount', 1);

    const user = await this.userRepository.findOne({ where: { id: userId } });
    return {
      id: saved.id,
      userId,
      nickname: user?.nickname ?? null,
      profileImageUrl: user?.profileImageUrl ?? null,
      content: saved.content,
      createdAt: saved.createdAt,
    };
  }

  async getComments(shortformId: number, query: ShortformCommentQueryDto) {
    await this.ensureShortform(shortformId);

    const [comments, total] = await this.shortformCommentRepository.findAndCount({
      where: { shortformId },
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const userIds = [...new Set(comments.map((comment) => comment.userId))];
    const users = userIds.length ? await this.userRepository.find({ where: { id: In(userIds) } }) : [];
    const userMap = new Map(users.map((item) => [item.id, item]));

    const items = comments.map((comment) => ({
      id: comment.id,
      userId: comment.userId,
      nickname: userMap.get(comment.userId)?.nickname ?? null,
      profileImageUrl: userMap.get(comment.userId)?.profileImageUrl ?? null,
      content: comment.content,
      createdAt: comment.createdAt,
    }));

    return new PaginationResponseDto(items, total, query.page, query.limit);
  }

  async getRanking(query: ShortformRankingQueryDto, viewerId?: number) {
    const now = new Date();
    const start = new Date(now);

    if (query.period === ShortformRankingPeriod.DAY) start.setDate(start.getDate() - 1);
    if (query.period === ShortformRankingPeriod.WEEK) start.setDate(start.getDate() - 7);
    if (query.period === ShortformRankingPeriod.MONTH) start.setMonth(start.getMonth() - 1);

    const items = await this.shortformRepository.find({
      where: { createdAt: LessThan(now) },
      order: { likeCount: 'DESC', viewCount: 'DESC', createdAt: 'DESC' },
      take: query.limit ?? 20,
    });

    const filtered = items.filter((item) => item.createdAt >= start);
    return this.mapShortforms(filtered, viewerId);
  }

  async removeShortform(userId: number, shortformId: number) {
    const shortform = await this.ensureShortform(shortformId);
    if (shortform.userId !== userId) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }

    await this.shortformLikeRepository.softDelete({ shortformId });
    await this.shortformCommentRepository.softDelete({ shortformId });
    await this.shortformProductRepository.softDelete({ shortformId });
    await this.shortformRepository.softDelete({ id: shortformId });

    return { success: true, message: '숏폼이 삭제되었습니다.' };
  }

  async getTranscodeStatus(shortformId: number) {
    const shortform = await this.ensureShortform(shortformId);
    return {
      shortformId: shortform.id,
      transcodeStatus: shortform.transcodeStatus,
      transcodedVideoUrl: shortform.transcodedVideoUrl,
      transcodeError: shortform.transcodeError,
      transcodedAt: shortform.transcodedAt,
    };
  }

  async retryTranscode(userId: number, shortformId: number) {
    const shortform = await this.ensureShortform(shortformId);
    if (shortform.userId !== userId) {
      throw new BusinessException('AUTH_FORBIDDEN', HttpStatus.FORBIDDEN);
    }

    await this.shortformRepository.update(shortform.id, {
      transcodeStatus: ShortformTranscodeStatus.PENDING,
      transcodeError: null,
    });

    await this.videoTranscodeQueue.add(
      'transcode',
      { shortformId: shortform.id },
      {
        attempts: 3,
        backoff: { type: 'fixed', delay: 1000 },
        removeOnComplete: true,
      },
    );

    return { success: true, message: '트랜스코딩 재시도 작업이 등록되었습니다.' };
  }

  async getUserShortforms(userId: number, query: ShortformFeedQueryDto, viewerId?: number) {
    const [items, total] = await this.shortformRepository.findAndCount({
      where: { userId },
      order: { createdAt: 'DESC' },
      skip: query.skip,
      take: query.limit,
    });

    const mapped = await this.mapShortforms(items, viewerId);
    return new PaginationResponseDto(mapped, total, query.page, query.limit);
  }

  private async mapShortforms(items: Shortform[], viewerId?: number) {
    if (!items.length) return [];

    const userIds = [...new Set(items.map((item) => item.userId))];
    const shortformIds = items.map((item) => item.id);

    const [users, products, likes] = await Promise.all([
      this.userRepository.find({ where: { id: In(userIds) } }),
      this.shortformProductRepository.find({ where: { shortformId: In(shortformIds) } }),
      viewerId
        ? this.shortformLikeRepository.find({ where: { userId: viewerId, shortformId: In(shortformIds) } })
        : Promise.resolve([]),
    ]);

    const userMap = new Map(users.map((user) => [user.id, user]));
    const productMap = new Map<number, number[]>();

    for (const item of products) {
      const current = productMap.get(item.shortformId) ?? [];
      current.push(item.productId);
      productMap.set(item.shortformId, current);
    }

    const likedSet = new Set(likes.map((like) => like.shortformId));

    return items.map((item) => ({
      id: item.id,
      userId: item.userId,
      nickname: userMap.get(item.userId)?.nickname ?? null,
      profileImageUrl: userMap.get(item.userId)?.profileImageUrl ?? null,
      title: item.title,
      videoUrl: item.videoUrl,
      thumbnailUrl: item.thumbnailUrl,
      durationSec: item.durationSec,
      viewCount: item.viewCount,
      likeCount: item.likeCount,
      commentCount: item.commentCount,
      transcodeStatus: item.transcodeStatus,
      transcodedVideoUrl: item.transcodedVideoUrl,
      transcodeError: item.transcodeError,
      transcodedAt: item.transcodedAt,
      productIds: productMap.get(item.id) ?? [],
      liked: viewerId ? likedSet.has(item.id) : false,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    }));
  }

  private async ensureUser(userId: number) {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new BusinessException('USER_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return user;
  }

  private async ensureShortform(shortformId: number) {
    const shortform = await this.shortformRepository.findOne({ where: { id: shortformId } });
    if (!shortform) {
      throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
    }
    return shortform;
  }

  private validateVideo(file: Express.Multer.File | undefined, durationSec?: number) {
    if (!file) {
      throw new BusinessException('FILE_UPLOAD_FAILED', HttpStatus.BAD_REQUEST, '업로드할 영상 파일이 필요합니다.');
    }

    if (!VideoService.ALLOWED_VIDEO_MIME.has(file.mimetype)) {
      throw new BusinessException('FILE_TYPE_NOT_ALLOWED', HttpStatus.BAD_REQUEST);
    }

    if (file.size > VideoService.MAX_VIDEO_SIZE) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '최대 200MB 영상만 업로드할 수 있습니다.');
    }

    if (durationSec !== undefined && durationSec > 60) {
      throw new BusinessException('VALIDATION_FAILED', HttpStatus.BAD_REQUEST, '숏폼 길이는 최대 60초입니다.');
    }
  }
}
