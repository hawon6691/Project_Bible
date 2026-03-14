import { Column, Entity, Index, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { User } from '../../user/entities/user.entity';

export enum SupportTicketStatus {
  OPEN = 'OPEN',
  ANSWERED = 'ANSWERED',
}

@Entity('support_tickets')
export class SupportTicket extends BaseEntity {
  @Index('idx_support_tickets_user_id')
  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @Index('idx_support_tickets_status')
  @Column({ type: 'enum', enum: SupportTicketStatus, default: SupportTicketStatus.OPEN })
  status: SupportTicketStatus;

  @Column({ type: 'varchar', length: 50 })
  category: string;

  @Column({ type: 'varchar', length: 120 })
  title: string;

  @Column({ type: 'text' })
  content: string;

  @Column({ name: 'attachment_url', type: 'varchar', length: 500, nullable: true })
  attachmentUrl: string | null;

  @Column({ name: 'answer_content', type: 'text', nullable: true })
  answerContent: string | null;

  @Column({ name: 'answered_by', type: 'int', nullable: true })
  answeredBy: number | null;

  @Column({ name: 'answered_at', type: 'timestamp', nullable: true })
  answeredAt: Date | null;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;
}
