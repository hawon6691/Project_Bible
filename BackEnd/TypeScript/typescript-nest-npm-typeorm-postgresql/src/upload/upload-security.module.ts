import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { SystemSetting } from '../admin-settings/entities/system-setting.entity';
import { UploadSecurityService } from './upload-security.service';

@Module({
  imports: [TypeOrmModule.forFeature([SystemSetting])],
  providers: [UploadSecurityService],
  exports: [UploadSecurityService],
})
export class UploadSecurityModule {}
