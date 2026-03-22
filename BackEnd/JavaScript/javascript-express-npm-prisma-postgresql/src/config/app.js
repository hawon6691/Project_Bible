export const appConfig = {
  appName: process.env.APP_NAME ?? "pbshop-javascript-express-npm-prisma-postgresql",
  apiPrefix: process.env.API_PREFIX ?? "/api/v1",
  port: Number(process.env.PORT ?? 8000),
  nodeEnv: process.env.NODE_ENV ?? "development",
};
