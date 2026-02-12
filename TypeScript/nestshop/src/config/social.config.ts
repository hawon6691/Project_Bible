import { registerAs } from '@nestjs/config';

export const socialConfig = registerAs('social', () => ({
  google: {
    clientId: process.env.GOOGLE_CLIENT_ID ?? '',
    clientSecret: process.env.GOOGLE_CLIENT_SECRET ?? '',
    callbackUrl: process.env.GOOGLE_CALLBACK_URL ?? 'http://localhost:3000/api/v1/auth/google/callback',
  },
  kakao: {
    clientId: process.env.KAKAO_CLIENT_ID ?? '',
    clientSecret: process.env.KAKAO_CLIENT_SECRET ?? '',
    callbackUrl: process.env.KAKAO_CALLBACK_URL ?? 'http://localhost:3000/api/v1/auth/kakao/callback',
  },
  naver: {
    clientId: process.env.NAVER_CLIENT_ID ?? '',
    clientSecret: process.env.NAVER_CLIENT_SECRET ?? '',
    callbackUrl: process.env.NAVER_CALLBACK_URL ?? 'http://localhost:3000/api/v1/auth/naver/callback',
  },
  facebook: {
    clientId: process.env.FACEBOOK_CLIENT_ID ?? '',
    clientSecret: process.env.FACEBOOK_CLIENT_SECRET ?? '',
    callbackUrl: process.env.FACEBOOK_CALLBACK_URL ?? 'http://localhost:3000/api/v1/auth/facebook/callback',
  },
  instagram: {
    clientId: process.env.INSTAGRAM_CLIENT_ID ?? '',
    clientSecret: process.env.INSTAGRAM_CLIENT_SECRET ?? '',
    callbackUrl: process.env.INSTAGRAM_CALLBACK_URL ?? 'http://localhost:3000/api/v1/auth/instagram/callback',
  },
}));
