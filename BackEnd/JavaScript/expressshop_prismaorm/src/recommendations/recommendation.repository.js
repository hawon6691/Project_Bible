import { prisma } from "../prisma.js";

function recommendationInclude() {
  return {
    product: {
      select: {
        id: true,
        name: true,
        description: true,
        price: true,
        discountPrice: true,
        status: true,
        stock: true,
        thumbnailUrl: true,
        lowestPrice: true,
        sellerCount: true,
        reviewCount: true,
        averageRating: true,
        popularityScore: true,
        category: {
          select: { id: true, name: true, parentId: true },
        },
      },
    },
  };
}

export function findTodayRecommendations(today) {
  return prisma.recommendation.findMany({
    where: {
      type: "TODAY",
      OR: [{ startDate: null }, { startDate: { lte: today } }],
      AND: [{ OR: [{ endDate: null }, { endDate: { gte: today } }] }],
    },
    include: recommendationInclude(),
    orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
  });
}

export function findRecentViewCategories(userId) {
  return prisma.recentProductView.findMany({
    where: { userId },
    include: {
      product: {
        select: { categoryId: true },
      },
    },
    orderBy: { viewedAt: "desc" },
    take: 10,
  });
}

export function findPersonalizedProducts(categoryIds, limit) {
  return prisma.product.findMany({
    where: {
      status: "ON_SALE",
      ...(categoryIds.length > 0 ? { categoryId: { in: categoryIds } } : {}),
    },
    include: {
      category: {
        select: { id: true, name: true, parentId: true },
      },
    },
    orderBy: [{ popularityScore: "desc" }, { salesCount: "desc" }, { id: "asc" }],
    take: limit,
  });
}

export function findAdminRecommendations() {
  return prisma.recommendation.findMany({
    include: recommendationInclude(),
    orderBy: [{ type: "asc" }, { sortOrder: "asc" }, { id: "asc" }],
  });
}

export function createRecommendation(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('recommendations', 'id'), COALESCE((SELECT MAX(id) FROM recommendations), 0) + 1, false)",
    );

    return tx.recommendation.create({
      data,
      include: recommendationInclude(),
    });
  });
}

export function deleteRecommendationById(id) {
  return prisma.recommendation.delete({
    where: { id: Number(id) },
    include: recommendationInclude(),
  });
}

export function findRecommendationById(id) {
  return prisma.recommendation.findUnique({
    where: { id: Number(id) },
    include: recommendationInclude(),
  });
}
