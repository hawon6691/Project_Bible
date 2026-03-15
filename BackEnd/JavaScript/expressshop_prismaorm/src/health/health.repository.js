import { prisma } from "../prisma.js";

export function pingDatabase() {
  return prisma.$queryRaw`SELECT 1`;
}
