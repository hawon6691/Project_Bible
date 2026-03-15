import { PrismaClient } from "@prisma/client";

import { appConfig } from "./config/app.js";

const globalForPrisma = globalThis;

export const prisma =
  globalForPrisma.prisma ??
  new PrismaClient({
    log: appConfig.nodeEnv === "development" ? ["warn", "error"] : ["error"],
  });

if (appConfig.nodeEnv !== "production") {
  globalForPrisma.prisma = prisma;
}
