import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { I18nController } from './i18n.controller';
import { I18nService } from './i18n.service';
import { ExchangeRate } from './entities/exchange-rate.entity';
import { Translation } from './entities/translation.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Translation, ExchangeRate])],
  controllers: [I18nController],
  providers: [I18nService],
  exports: [I18nService],
})
export class I18nModule {}
