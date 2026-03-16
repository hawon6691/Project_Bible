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
    },
  });
}

export function findCompareItem(compareKey, productId) {
  return prisma.compareItem.findUnique({
    where: {
      compareKey_productId: {
        compareKey,
        productId: Number(productId),
      },
    },
  });
}

export function findCompareItems(compareKey) {
  return prisma.compareItem.findMany({
    where: { compareKey },
    include: {
      product: {
        select: {
          id: true,
          name: true,
          categoryId: true,
          price: true,
          discountPrice: true,
          lowestPrice: true,
          averageRating: true,
          reviewCount: true,
          sellerCount: true,
          salesCount: true,
          thumbnailUrl: true,
        },
      },
    },
    orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
  });
}

export function createCompareItem(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('compare_items', 'id'), COALESCE((SELECT MAX(id) FROM compare_items), 0) + 1, false)",
    );
    return tx.compareItem.create({
      data,
    });
  });
}

export function deleteCompareItem(compareKey, productId) {
  return prisma.compareItem.deleteMany({
    where: {
      compareKey,
      productId: Number(productId),
    },
  });
}
