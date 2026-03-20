import { prisma } from "../prisma.js";

export function findProductQueryViews(filters = {}) {
  const keyword = String(filters.keyword ?? "").trim();
  const categoryId = filters.categoryId === null || filters.categoryId === undefined
    ? null
    : Number(filters.categoryId);

  return prisma.productQueryView.findMany({
    where: {
      ...(categoryId ? { categoryId } : {}),
      ...(keyword
        ? {
            name: {
              contains: keyword,
              mode: "insensitive",
            },
          }
        : {}),
    },
    select: {
      productId: true,
      categoryId: true,
      name: true,
      thumbnailUrl: true,
      status: true,
      basePrice: true,
      lowestPrice: true,
      sellerCount: true,
      averageRating: true,
      reviewCount: true,
      viewCount: true,
      popularityScore: true,
      syncedAt: true,
      updatedAt: true,
    },
  });
}

export function findProductQueryViewByProductId(productId) {
  return prisma.productQueryView.findUnique({
    where: {
      productId: Number(productId),
    },
    select: {
      productId: true,
      categoryId: true,
      name: true,
      thumbnailUrl: true,
      status: true,
      basePrice: true,
      lowestPrice: true,
      sellerCount: true,
      averageRating: true,
      reviewCount: true,
      viewCount: true,
      popularityScore: true,
      syncedAt: true,
      updatedAt: true,
    },
  });
}
