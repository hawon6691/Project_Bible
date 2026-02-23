import { Process, Processor } from '@nestjs/bull';
import { Injectable } from '@nestjs/common';
import { Job } from 'bull';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { basename, dirname, join } from 'path';
import { existsSync, mkdirSync } from 'fs';
import { spawn } from 'child_process';
import { ConfigService } from '@nestjs/config';
import { Shortform, ShortformTranscodeStatus } from './entities/shortform.entity';

export interface VideoTranscodeJobData {
  shortformId: number;
}

@Processor('video-transcode')
@Injectable()
export class VideoProcessor {
  constructor(
    @InjectRepository(Shortform)
    private readonly shortformRepository: Repository<Shortform>,
    private readonly configService: ConfigService,
  ) {}

  // 큐 워커에서 FFmpeg를 실행해 숏폼 mp4/썸네일을 생성한다.
  @Process('transcode')
  async transcode(job: Job<VideoTranscodeJobData>) {
    const shortform = await this.shortformRepository.findOne({ where: { id: job.data.shortformId } });
    if (!shortform) return;

    try {
      await this.shortformRepository.update(shortform.id, {
        transcodeStatus: ShortformTranscodeStatus.PROCESSING,
        transcodeError: null,
      });

      const inputPath = this.toAbsolutePath(shortform.videoUrl);
      if (!existsSync(inputPath)) {
        throw new Error(`원본 파일을 찾을 수 없습니다: ${inputPath}`);
      }

      const fileBaseName = basename(inputPath).replace(/\.[^/.]+$/, '');
      const outputPath = join(process.cwd(), 'uploads', 'shortforms', 'transcoded', `${fileBaseName}.mp4`);
      const thumbnailPath = join(process.cwd(), 'uploads', 'shortforms', 'thumb', `${fileBaseName}.jpg`);
      mkdirSync(dirname(outputPath), { recursive: true });
      mkdirSync(dirname(thumbnailPath), { recursive: true });

      await this.runFfmpeg([
        '-y',
        '-i',
        inputPath,
        '-vf',
        "scale='min(720,iw)':-2:force_original_aspect_ratio=decrease,pad=720:1280:(ow-iw)/2:(oh-ih)/2",
        '-c:v',
        'libx264',
        '-preset',
        this.configService.get<string>('FFMPEG_PRESET', 'veryfast'),
        '-crf',
        String(this.configService.get<number>('FFMPEG_CRF', 28)),
        '-movflags',
        '+faststart',
        '-c:a',
        'aac',
        '-b:a',
        '128k',
        outputPath,
      ]);

      await this.runFfmpeg(['-y', '-i', outputPath, '-ss', '00:00:01.000', '-vframes', '1', thumbnailPath]);

      await this.shortformRepository.update(shortform.id, {
        transcodeStatus: ShortformTranscodeStatus.COMPLETED,
        transcodedVideoUrl: this.toPublicUrl(outputPath),
        thumbnailUrl: this.toPublicUrl(thumbnailPath),
        transcodeError: null,
        transcodedAt: new Date(),
      });
    } catch (error) {
      await this.shortformRepository.update(shortform.id, {
        transcodeStatus: ShortformTranscodeStatus.FAILED,
        transcodeError: error instanceof Error ? error.message : 'Transcode failed',
      });
      throw error;
    }
  }

  private async runFfmpeg(args: string[]) {
    const ffmpegBinary = this.configService.get<string>('FFMPEG_BIN', 'ffmpeg');

    await new Promise<void>((resolve, reject) => {
      const child = spawn(ffmpegBinary, args, { stdio: ['ignore', 'ignore', 'pipe'] });
      let stderr = '';

      child.stderr.on('data', (chunk: Buffer) => {
        stderr += chunk.toString();
      });

      child.on('error', (error) => {
        reject(error);
      });

      child.on('close', (code) => {
        if (code === 0) {
          resolve();
          return;
        }
        reject(new Error(stderr || `ffmpeg exited with code ${code}`));
      });
    });
  }

  private toAbsolutePath(publicUrl: string) {
    const segments = publicUrl.replace(/^\//, '').split('/').filter(Boolean);
    return join(process.cwd(), ...segments);
  }

  private toPublicUrl(absolutePath: string) {
    const normalized = absolutePath.replace(process.cwd(), '').replace(/\\/g, '/');
    return normalized.startsWith('/') ? normalized : `/${normalized}`;
  }
}
