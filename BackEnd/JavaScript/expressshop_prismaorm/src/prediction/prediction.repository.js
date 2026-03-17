import { prisma } from "../prisma.js";

export function findProductById(productId) {
  return prisma.product.findFirst({
    where: {
      id: Number(productId),
      deletedAt: null,
    },
    select: {
      id: true,
      name: true,
      price: true,
      discountPrice: true,
      lowestPrice: true,
    },
  });
}

export function findPricePredictions(productId, endDate) {
  return prisma.pricePrediction.findMany({
    where: {
      productId: Number(productId),
      predictionDate: {
        gte: new Date(new Date().toISOString().slice(0, 10)),
        lte: endDate,
      },
    },
    orderBy: [{ predictionDate: "asc" }, { id: "asc" }],
  });
}

export function findLatestPricePrediction(productId) {
  return prisma.pricePrediction.findFirst({
    where: {
      productId: Number(productId),
    },
    orderBy: [{ calculatedAt: "desc" }, { predictionDate: "asc" }, { id: "asc" }],
  });
}

export function findRecentPriceHistory(productId, limit = 30) {
  return prisma.priceHistory.findMany({
    where: {
      productId: Number(productId),
    },
    orderBy: [{ date: "desc" }, { id: "desc" }],
    take: limit,
    select: {
      date: true,
      lowestPrice: true,
      averagePrice: true,
      highestPrice: true,
    },
  });
}
