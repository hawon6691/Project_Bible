import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { readFile } from 'fs/promises';
import { extname } from 'path';
import { Repository } from 'typeorm';
import { SystemSetting } from '../admin-settings/entities/system-setting.entity';
import { CacheService } from '../common/cache/cache.service';
import { BusinessException } from '../common/exceptions/business.exception';

type SupportedFileType = 'jpg' | 'jpeg' | 'png' | 'webp' | 'gif' | 'mp4' | 'mov' | 'mp3' | 'wav' | 'pdf';

@Injectable()
export class UploadSecurityService {
  private static readonly DEFAULT_ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp', 'gif', 'mp4', 'mp3', 'pdf'];
  private static readonly SETTINGS_CACHE_KEY = 'settings:allowed_extensions';

  constructor(
    @InjectRepository(SystemSetting)
    private readonly systemSettingRepository: Repository<SystemSetting>,
    private readonly cacheService: CacheService,
  ) {}

  // 파일 확장자 + Magic Number를 모두 확인해 MIME 위조 업로드를 차단한다.
  async validateFile(file: Express.Multer.File | undefined) {
    if (!file) {
      throw new BusinessException('FILE_UPLOAD_FAILED', HttpStatus.BAD_REQUEST, '업로드할 파일이 필요합니다.');
    }

    const extension = this.getFileExtension(file.originalname);
    const allowedExtensions = await this.getAllowedExtensions();
    if (!allowedExtensions.includes(extension)) {
      throw new BusinessException('FILE_TYPE_NOT_ALLOWED', HttpStatus.BAD_REQUEST, '허용되지 않은 확장자입니다.');
    }

    const header = await this.readHeader(file);
    const detected = this.detectFileType(header, file.originalname);

    if (!detected) {
      throw new BusinessException('FILE_TYPE_NOT_ALLOWED', HttpStatus.BAD_REQUEST, '파일 시그니처를 확인할 수 없습니다.');
    }

    if (!this.isExtensionMatch(extension, detected)) {
      throw new BusinessException('FILE_TYPE_NOT_ALLOWED', HttpStatus.BAD_REQUEST, '확장자와 파일 시그니처가 일치하지 않습니다.');
    }
  }

  private async getAllowedExtensions() {
    const cached = await this.cacheService.getJson<{ extensions?: string[] }>(UploadSecurityService.SETTINGS_CACHE_KEY);
    if (cached?.extensions?.length) {
      return this.normalizeExtensions(cached.extensions);
    }

    const setting = await this.systemSettingRepository.findOne({ where: { settingKey: 'allowed_extensions' } });
    const extensions = this.normalizeExtensions(
      Array.isArray(setting?.settingValue?.extensions)
        ? (setting?.settingValue?.extensions as string[])
        : UploadSecurityService.DEFAULT_ALLOWED_EXTENSIONS,
    );

    await this.cacheService.setJson(
      UploadSecurityService.SETTINGS_CACHE_KEY,
      { extensions },
      60 * 5,
    );

    return extensions;
  }

  private normalizeExtensions(extensions: string[]) {
    return [...new Set(extensions.map((item) => item.trim().toLowerCase()).filter(Boolean))];
  }

  private getFileExtension(filename: string) {
    const ext = extname(filename).replace('.', '').toLowerCase();
    return ext || 'bin';
  }

  private async readHeader(file: Express.Multer.File) {
    if (file.buffer && file.buffer.length > 0) {
      return file.buffer.subarray(0, 16);
    }

    if (file.path) {
      const buf = await readFile(file.path);
      return buf.subarray(0, 16);
    }

    throw new BusinessException('FILE_UPLOAD_FAILED', HttpStatus.BAD_REQUEST, '파일 내용을 읽을 수 없습니다.');
  }

  private detectFileType(header: Buffer, originalname: string): SupportedFileType | null {
    const hex = header.toString('hex').toLowerCase();
    const ascii = header.toString('ascii');
    const ext = this.getFileExtension(originalname);

    if (hex.startsWith('ffd8ff')) return 'jpg';
    if (hex.startsWith('89504e470d0a1a0a')) return 'png';
    if (ascii.startsWith('GIF87a') || ascii.startsWith('GIF89a')) return 'gif';
    if (ascii.startsWith('RIFF') && header.subarray(8, 12).toString('ascii') === 'WEBP') return 'webp';
    if (ascii.startsWith('%PDF-')) return 'pdf';
    if (ascii.startsWith('RIFF') && header.subarray(8, 12).toString('ascii') === 'WAVE') return 'wav';
    if (ascii.startsWith('ID3') || hex.startsWith('fffb') || hex.startsWith('fff3') || hex.startsWith('fff2')) return 'mp3';
    if (header.length >= 8 && header.subarray(4, 8).toString('ascii') === 'ftyp') {
      if (ext === 'mov') return 'mov';
      return 'mp4';
    }

    return null;
  }

  private isExtensionMatch(extension: string, detected: SupportedFileType) {
    if (extension === 'jpeg' && detected === 'jpg') return true;
    if (extension === 'jpg' && detected === 'jpeg') return true;
    if (extension === 'mov' && detected === 'mp4') return true;
    return extension === detected;
  }
}
