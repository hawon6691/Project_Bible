import {
  Entity,
  Column,
  Index,
  ManyToOne,
  JoinColumn,
  CreateDateColumn,
  PrimaryGeneratedColumn,
} from 'typeorm';
import { User } from '../../user/entities/user.entity';

export enum VerificationType {
  SIGNUP = 'SIGNUP',
  PASSWORD_RESET = 'PASSWORD_RESET',
}

@Entity('email_verifications')
@Index('idx_email_verifications_user_type', ['userId', 'type'])
@Index('idx_email_verifications_code', ['userId', 'code', 'type'])
export class EmailVerification {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'user_id', type: 'int' })
  userId: number;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ type: 'enum', enum: VerificationType })
  type: VerificationType;

  @Column({ type: 'varchar', length: 6 })
  code: string;

  @Column({ name: 'attempt_count', type: 'int', default: 0 })
  attemptCount: number;

  @Column({ name: 'is_used', type: 'boolean', default: false })
  isUsed: boolean;

  @Index('idx_email_verifications_expires')
  @Column({ name: 'expires_at', type: 'timestamp' })
  expiresAt: Date;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;
}
