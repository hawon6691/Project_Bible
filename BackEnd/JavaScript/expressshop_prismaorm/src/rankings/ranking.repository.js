import { prisma } from "../prisma.js";

export function findPopularProducts({ categoryId, limit }) {
  return prisma.product.findMany({
    where: {
      status: "ON_SALE",
      ...(categoryId ? { categoryId: Number(categoryId) } : {}),
    },
    select: {
      id: true,
      name: true,
      lowestPrice: true,
      thumbnailUrl: true,
      popularityScore: true,
      salesCount: true,
      reviewCount: true,
      viewCount: true,
    },
    orderBy: [{ popularityScore: "desc" }, { salesCount: "desc" }, { id: "asc" }],
    take: limit,
  });
}

export function findPopularSearchKeywords(limit) {
  return prisma.searchLog.groupBy({
    by: ["keyword"],
    _count: { keyword: true },
    orderBy: { _count: { keyword: "desc" } },
    take: limit,
  });
}

export function findProductsForRecalculation() {
  return prisma.product.findMany({
    select: {
      id: true,
      salesCount: true,
      reviewCount: true,
      viewCount: true,
      averageRating: true,
    },
  });
}

export async function recalculatePopularityScores(products) {
  const operations = products.map((product) => {
    const ratingScore = Math.round(Number(product.averageRating ?? 0) * 100);
    const nextScore =
      Number(product.salesCount ?? 0) * 10 +
      Number(product.reviewCount ?? 0) * 5 +
      Number(product.viewCount ?? 0) +
      ratingScore;

    return prisma.product.update({
      where: { id: product.id },
      data: { popularityScore: nextScore },
    });
  });

  await prisma.$transaction(operations);
}
