import { Column, Entity, Index, JoinColumn, ManyToOne, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { Shortform } from './shortform.entity';

@Entity('shortform_likes')
@Unique('uq_shortform_likes_shortform_user', ['shortformId', 'userId'])
export class ShortformLike extends BaseEntity {
  @Index('idx_shortform_likes_shortform_id')
  @Column({ name: 'shortform_id', type: 'int' })
  shortformId: number;

  @Index('idx_shortform_likes_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @ManyToOne(() => Shortform, (shortform) => shortform.likes, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'shortform_id' })
  shortform: Shortform;
}
