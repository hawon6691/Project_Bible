import { registerAs } from '@nestjs/config';

export const imageConfig = registerAs('image', () => ({
  maxFileSize: 10 * 1024 * 1024, // 10MB
  allowedMimeTypes: ['image/jpeg', 'image/png', 'image/webp', 'image/gif'],
  thumbnailSize: { width: 150, height: 150 },
  mediumSize: { width: 600, height: 600 },
  largeSize: { width: 1200, height: 1200 },
  webpQuality: 80,
}));
