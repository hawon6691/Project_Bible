import { prisma } from "../prisma.js";

export function findUserByEmail(email) {
  return prisma.user.findUnique({ where: { email } });
}

export function findUserById(id) {
  return prisma.user.findUnique({ where: { id: Number(id) } });
}

export function createUser(data) {
  return prisma.user.create({ data });
}

export function updateUserRefreshToken(userId, refreshToken) {
  return prisma.user.update({
    where: { id: Number(userId) },
    data: { refreshToken },
  });
}
