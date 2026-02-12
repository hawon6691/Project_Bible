import { registerAs } from '@nestjs/config';

export const websocketConfig = registerAs('websocket', () => ({
  cors: {
    origin: process.env.WS_CORS_ORIGIN ?? '*',
    credentials: true,
  },
  pingInterval: 25000,
  pingTimeout: 20000,
}));
