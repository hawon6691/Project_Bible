import { prisma } from "../prisma.js";

export function findUserByEmail(email) {
  return prisma.user.findUnique({ where: { email } });
}

export function findUserByNickname(nickname) {
  return prisma.user.findUnique({ where: { nickname } });
}

export function findUserById(id) {
  return prisma.user.findUnique({ where: { id: Number(id) } });
}

export function createUser(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 0) + 1, false)",
    );
    return tx.user.create({ data });
  });
}

export function updateUserRefreshToken(userId, refreshToken) {
  return prisma.user.update({
    where: { id: Number(userId) },
    data: { refreshToken },
  });
}

export function updateUserAccount(userId, data) {
  return prisma.user.update({
    where: { id: Number(userId) },
    data,
  });
}

export function createEmailVerification(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('email_verifications', 'id'), COALESCE((SELECT MAX(id) FROM email_verifications), 0) + 1, false)",
    );
    return tx.emailVerification.create({ data });
  });
}

export function markEmailVerificationsUsed(userId, type) {
  return prisma.emailVerification.updateMany({
    where: {
      userId: Number(userId),
      type,
      isUsed: false,
    },
    data: { isUsed: true },
  });
}

export function findActiveEmailVerification(userId, type, code) {
  return prisma.emailVerification.findFirst({
    where: {
      userId: Number(userId),
      type,
      ...(code ? { code } : {}),
      isUsed: false,
    },
    orderBy: { createdAt: "desc" },
  });
}

export function incrementEmailVerificationAttempts(id) {
  return prisma.emailVerification.update({
    where: { id: Number(id) },
    data: {
      attemptCount: { increment: 1 },
    },
  });
}
