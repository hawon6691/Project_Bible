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
      description: true,
      price: true,
      discountPrice: true,
      lowestPrice: true,
    },
  });
}

export function findLowestPriceHistory(productId) {
  return prisma.priceHistory.findFirst({
    where: { productId: Number(productId) },
    orderBy: [{ lowestPrice: "asc" }, { date: "asc" }],
    select: {
      lowestPrice: true,
      date: true,
    },
  });
}
