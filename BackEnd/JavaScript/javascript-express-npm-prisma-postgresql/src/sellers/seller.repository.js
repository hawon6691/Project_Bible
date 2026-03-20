import { prisma } from "../prisma.js";

export function findSellers(page, limit) {
  return Promise.all([
    prisma.seller.findMany({
      skip: (page - 1) * limit,
      take: limit,
      orderBy: { id: "asc" },
    }),
    prisma.seller.count(),
  ]);
}

export function findSellerById(sellerId) {
  return prisma.seller.findUnique({
    where: { id: Number(sellerId) },
  });
}

export function createSeller(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('sellers', 'id'), COALESCE((SELECT MAX(id) FROM sellers), 0) + 1, false)",
    );
    return tx.seller.create({
      data,
    });
  });
}

export function updateSeller(sellerId, data) {
  return prisma.seller.update({
    where: { id: Number(sellerId) },
    data,
  });
}

export function deleteSeller(sellerId) {
  return prisma.seller.delete({
    where: { id: Number(sellerId) },
  });
}

export function findSellerTrustMetric(sellerId) {
  return prisma.sellerTrustMetric.findUnique({
    where: { sellerId: Number(sellerId) },
  });
}

export function countSellerOrderItems(sellerId) {
  return prisma.orderItem.count({
    where: { sellerId: Number(sellerId) },
  });
}

export function getSellerReviewAggregates(sellerId) {
  return prisma.sellerReview.aggregate({
    where: {
      sellerId: Number(sellerId),
      deletedAt: null,
    },
    _avg: {
      rating: true,
      deliveryRating: true,
    },
    _count: {
      id: true,
    },
  });
}

export function findSellerReviews(sellerId, page, limit, sort) {
  const orderBy =
    sort === "rating_desc"
      ? [{ rating: "desc" }, { createdAt: "desc" }]
      : sort === "rating_asc"
        ? [{ rating: "asc" }, { createdAt: "desc" }]
        : [{ createdAt: "desc" }, { id: "desc" }];

  const where = {
    sellerId: Number(sellerId),
    deletedAt: null,
  };

  return Promise.all([
    prisma.sellerReview.findMany({
      where,
      include: {
        user: {
          select: {
            id: true,
            name: true,
            nickname: true,
          },
        },
      },
      skip: (page - 1) * limit,
      take: limit,
      orderBy,
    }),
    prisma.sellerReview.count({ where }),
  ]);
}

export function findSellerReviewById(reviewId) {
  return prisma.sellerReview.findUnique({
    where: { id: Number(reviewId) },
    include: {
      user: {
        select: {
          id: true,
          name: true,
          nickname: true,
        },
      },
    },
  });
}

export function findUserOrderForSellerReview(userId, orderId, sellerId) {
  return prisma.order.findFirst({
    where: {
      id: Number(orderId),
      userId: Number(userId),
      orderItems: {
        some: {
          sellerId: Number(sellerId),
        },
      },
    },
    select: {
      id: true,
    },
  });
}

export function createSellerReview(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('seller_reviews', 'id'), COALESCE((SELECT MAX(id) FROM seller_reviews), 0) + 1, false)",
    );
    return tx.sellerReview.create({
      data,
      include: {
        user: {
          select: {
            id: true,
            name: true,
            nickname: true,
          },
        },
      },
    });
  });
}

export function updateSellerReview(reviewId, data) {
  return prisma.sellerReview.update({
    where: { id: Number(reviewId) },
    data: {
      ...data,
      updatedAt: new Date(),
    },
    include: {
      user: {
        select: {
          id: true,
          name: true,
          nickname: true,
        },
      },
    },
  });
}

export function softDeleteSellerReview(reviewId) {
  return prisma.sellerReview.update({
    where: { id: Number(reviewId) },
    data: {
      deletedAt: new Date(),
      updatedAt: new Date(),
    },
  });
}

export function upsertSellerTrustMetric(sellerId, data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('seller_trust_metrics', 'id'), COALESCE((SELECT MAX(id) FROM seller_trust_metrics), 0) + 1, false)",
    );

    return tx.sellerTrustMetric.upsert({
      where: { sellerId: Number(sellerId) },
      update: {
        ...data,
        updatedAt: new Date(),
      },
      create: {
        sellerId: Number(sellerId),
        ...data,
      },
    });
  });
}
