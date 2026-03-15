import { prisma } from "../prisma.js";

export function findPointTransactions(userId) {
  return prisma.pointTransaction.findMany({
    where: { userId },
    orderBy: { id: "desc" },
  });
}

export function findUserById(userId) {
  return prisma.user.findUnique({ where: { id: Number(userId) } });
}

export function updateUserPoint(userId, point) {
  return prisma.user.update({
    where: { id: Number(userId) },
    data: { point },
  });
}

export function createPointTransaction(data) {
  return prisma.pointTransaction.create({ data });
}
