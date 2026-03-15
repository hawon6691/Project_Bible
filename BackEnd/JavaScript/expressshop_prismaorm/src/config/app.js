export const appConfig = {
  appName: process.env.APP_NAME ?? "javascript-express-prisma",
  apiPrefix: process.env.API_PREFIX ?? "/api/v1",
  port: Number(process.env.PORT ?? 8000),
  nodeEnv: process.env.NODE_ENV ?? "development",
};
