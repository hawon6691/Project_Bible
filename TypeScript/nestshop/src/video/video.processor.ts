import { Process, Processor } from '@nestjs/bull';
import { Job } from 'bull';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Shortform, ShortformTranscodeStatus } from './entities/shortform.entity';

export interface VideoTranscodeJobData {
  shortformId: number;
}

@Processor('video-transcode')
export class VideoProcessor {
  constructor(
    @InjectRepository(Shortform)
    private readonly shortformRepository: Repository<Shortform>,
  ) {}

  // 실제 FFmpeg 연동 전 단계: 큐 워커에서 트랜스코딩 상태/결과를 갱신한다.
  @Process('transcode')
  async transcode(job: Job<VideoTranscodeJobData>) {
    const shortform = await this.shortformRepository.findOne({ where: { id: job.data.shortformId } });
    if (!shortform) return;

    try {
      await this.shortformRepository.update(shortform.id, {
        transcodeStatus: ShortformTranscodeStatus.PROCESSING,
        transcodeError: null,
      });

      // 워커 처리 시뮬레이션 (향후 FFmpeg 변환 로직으로 교체)
      await new Promise((resolve) => setTimeout(resolve, 150));

      await this.shortformRepository.update(shortform.id, {
        transcodeStatus: ShortformTranscodeStatus.COMPLETED,
        transcodedVideoUrl: shortform.videoUrl,
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
}
