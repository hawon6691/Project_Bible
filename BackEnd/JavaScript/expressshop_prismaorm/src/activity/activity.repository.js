import { prisma } from "../prisma.js";

function viewInclude() {
  return {
    product: {
      select: {
        id: true,
        name: true,
        price: true,
        discountPrice: true,
        status: true,
        thumbnailUrl: true,
      },
    },
  };
}

export function findRecentViews(userId, page = 1, limit = 20) {
  const skip = (page - 1) * limit;
  return prisma.recentProductView.findMany({
    where: { userId },
    include: viewInclude(),
    orderBy: [{ viewedAt: "desc" }, { id: "desc" }],
    skip,
    take: limit,
  });
}

export function countRecentViews(userId) {
  return prisma.recentProductView.count({ where: { userId } });
}

export function deleteRecentViews(userId) {
  return prisma.recentProductView.deleteMany({ where: { userId } });
}

export function findSearchHistories(userId) {
  return prisma.searchHistory.findMany({
    where: { userId },
    orderBy: [{ createdAt: "desc" }, { id: "desc" }],
  });
}

export function countSearchHistories(userId) {
  return prisma.searchHistory.count({ where: { userId } });
}

export function deleteSearchHistories(userId) {
  return prisma.searchHistory.deleteMany({ where: { userId } });
}

export function deleteSearchHistoryById(userId, id) {
  return prisma.searchHistory.deleteMany({
    where: { userId, id: Number(id) },
  });
}
