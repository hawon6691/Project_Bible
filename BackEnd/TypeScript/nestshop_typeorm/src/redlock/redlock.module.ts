import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { RedlockService } from './redlock.service';

@Module({
  imports: [ConfigModule],
  providers: [RedlockService],
  exports: [RedlockService],
})
export class RedlockModule {}
