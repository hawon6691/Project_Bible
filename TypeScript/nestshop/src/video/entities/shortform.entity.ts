import { Column, Entity, Index, OneToMany } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { ShortformComment } from './shortform-comment.entity';
import { ShortformLike } from './shortform-like.entity';
import { ShortformProduct } from './shortform-product.entity';

@Entity('shortforms')
export class Shortform extends BaseEntity {
  @Index('idx_shortforms_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ type: 'varchar', length: 120 })
  title: string;

  @Column({ name: 'video_url', type: 'varchar', length: 500 })
  videoUrl: string;

  @Column({ name: 'thumbnail_url', type: 'varchar', length: 500, nullable: true })
  thumbnailUrl: string | null;

  @Column({ name: 'duration_sec', type: 'int', default: 0 })
  durationSec: number;

  @Column({ name: 'view_count', type: 'int', default: 0 })
  viewCount: number;

  @Column({ name: 'like_count', type: 'int', default: 0 })
  likeCount: number;

  @Column({ name: 'comment_count', type: 'int', default: 0 })
  commentCount: number;

  @OneToMany(() => ShortformLike, (like) => like.shortform)
  likes: ShortformLike[];

  @OneToMany(() => ShortformComment, (comment) => comment.shortform)
  comments: ShortformComment[];

  @OneToMany(() => ShortformProduct, (item) => item.shortform)
  products: ShortformProduct[];
}
