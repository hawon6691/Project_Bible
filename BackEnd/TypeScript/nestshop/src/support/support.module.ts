import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../user/entities/user.entity';
import { SupportController } from './support.controller';
import { SupportService } from './support.service';
import { SupportTicket } from './entities/support-ticket.entity';

@Module({
  imports: [TypeOrmModule.forFeature([SupportTicket, User])],
  controllers: [SupportController],
  providers: [SupportService],
  exports: [SupportService],
})
export class SupportModule {}
