import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { FaqController } from './faq.controller';
import { FaqService } from './faq.service';
import { Faq } from './entities/faq.entity';
import { Notice } from './entities/notice.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Faq, Notice])],
  controllers: [FaqController],
  providers: [FaqService],
  exports: [FaqService],
})
export class FaqModule {}
