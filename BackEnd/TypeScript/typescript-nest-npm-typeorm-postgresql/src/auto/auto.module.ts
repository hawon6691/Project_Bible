import { Module } from '@nestjs/common';
import { AutoController } from './auto.controller';
import { AutoService } from './auto.service';

@Module({
  controllers: [AutoController],
  providers: [AutoService],
  exports: [AutoService],
})
export class AutoModule {}
