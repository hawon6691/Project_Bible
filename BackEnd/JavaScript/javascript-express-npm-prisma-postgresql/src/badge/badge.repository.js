import { prisma } from "../prisma.js";

export function findActiveBadges() {
  return prisma.badge.findMany({
    where: { isActive: true },
    orderBy: { id: "asc" },
  });
}

export function findBadgeById(badgeId) {
  return prisma.badge.findUnique({
    where: { id: Number(badgeId) },
  });
}

export function findBadgeByName(name) {
  return prisma.badge.findUnique({
    where: { name },
  });
}

export function findUserById(userId) {
  return prisma.user.findUnique({
    where: { id: Number(userId) },
  });
}

export function findUserBadges(userId) {
  return prisma.userBadge.findMany({
    where: {
      userId: Number(userId),
      badge: {
        isActive: true,
      },
    },
    include: {
      badge: true,
    },
    orderBy: [{ grantedAt: "desc" }, { id: "desc" }],
  });
}

export async function createBadge(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('badges', 'id'), COALESCE((SELECT MAX(id) FROM badges), 0) + 1, false)",
    );

    return tx.badge.create({
      data,
    });
  });
}

export function updateBadge(badgeId, data) {
  return prisma.badge.update({
    where: { id: Number(badgeId) },
    data: {
      ...data,
      updatedAt: new Date(),
    },
  });
}

export function createUserBadge(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('user_badges', 'id'), COALESCE((SELECT MAX(id) FROM user_badges), 0) + 1, false)",
    );

    return tx.userBadge.create({
      data,
      include: {
        badge: true,
      },
    });
  });
}

export function findUserBadge(badgeId, userId) {
  return prisma.userBadge.findFirst({
    where: {
      badgeId: Number(badgeId),
      userId: Number(userId),
    },
    include: {
      badge: true,
    },
  });
}

export function deleteUserBadge(badgeId, userId) {
  return prisma.userBadge.deleteMany({
    where: {
      badgeId: Number(badgeId),
      userId: Number(userId),
    },
  });
}

export function deleteUserBadgesByBadgeId(badgeId) {
  return prisma.userBadge.deleteMany({
    where: {
      badgeId: Number(badgeId),
    },
  });
}

export function countUserBadges(badgeId) {
  return prisma.userBadge.count({
    where: {
      badgeId: Number(badgeId),
    },
  });
}

export function updateBadgeHolderCount(badgeId, holderCount) {
  return prisma.badge.update({
    where: { id: Number(badgeId) },
    data: {
      holderCount: Number(holderCount),
      updatedAt: new Date(),
    },
  });
}

export async function deactivateBadgeAndDeleteUserBadges(badgeId) {
  return prisma.$transaction(async (tx) => {
    await tx.userBadge.deleteMany({
      where: {
        badgeId: Number(badgeId),
      },
    });

    return tx.badge.update({
      where: { id: Number(badgeId) },
      data: {
        isActive: false,
        holderCount: 0,
        updatedAt: new Date(),
      },
    });
  });
}
