import { registerAs } from '@nestjs/config';

export const bullConfig = registerAs('bull', () => ({
  redis: {
    host: process.env.BULL_REDIS_HOST ?? process.env.REDIS_HOST ?? 'localhost',
    port: parseInt(process.env.BULL_REDIS_PORT ?? process.env.REDIS_PORT ?? '6379', 10),
    password: process.env.REDIS_PASSWORD || undefined,
  },
}));
