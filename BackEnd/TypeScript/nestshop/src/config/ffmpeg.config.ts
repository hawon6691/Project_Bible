import { registerAs } from '@nestjs/config';

export const ffmpegConfig = registerAs('ffmpeg', () => ({
  maxFileSize: 500 * 1024 * 1024, // 500MB
  allowedMimeTypes: ['video/mp4', 'video/quicktime', 'video/webm'],
  outputFormat: 'mp4',
  maxDuration: 60, // 숏폼 최대 60초
  resolutions: {
    low: { width: 480, height: 854 },
    medium: { width: 720, height: 1280 },
    high: { width: 1080, height: 1920 },
  },
}));
