import { Column, Entity, Index, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { Shortform } from './shortform.entity';

@Entity('shortform_comments')
export class ShortformComment extends BaseEntity {
  @Index('idx_shortform_comments_shortform_id')
  @Column({ name: 'shortform_id', type: 'int' })
  shortformId: number;

  @Index('idx_shortform_comments_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Column({ type: 'varchar', length: 500 })
  content: string;

  @ManyToOne(() => Shortform, (shortform) => shortform.comments, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'shortform_id' })
  shortform: Shortform;
}
