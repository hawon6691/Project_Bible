import { prisma } from "../prisma.js";

export function updateUser(userId, data) {
  return prisma.user.update({
    where: { id: Number(userId) },
    data,
  });
}

export function findUsers(where, page, limit) {
  return Promise.all([
    prisma.user.findMany({
      where,
      skip: (page - 1) * limit,
      take: limit,
      orderBy: { id: "asc" },
    }),
    prisma.user.count({ where }),
  ]);
}

export function findUserProfile(userId) {
  return prisma.user.findUnique({
    where: { id: Number(userId) },
    select: {
      id: true,
      name: true,
      nickname: true,
      bio: true,
      profileImageUrl: true,
      role: true,
      createdAt: true,
    },
  });
}
