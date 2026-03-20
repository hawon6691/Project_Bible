import { prisma } from "../prisma.js";

export function findWishlistItem(userId, productId) {
  return prisma.wishlist.findFirst({
    where: { userId, productId: Number(productId) },
  });
}

export function findWishlist(userId, page, limit) {
  return Promise.all([
    prisma.wishlist.findMany({
      where: { userId },
      include: {
        product: {
          select: {
            id: true,
            name: true,
            thumbnailUrl: true,
            lowestPrice: true,
            averageRating: true,
          },
        },
      },
      skip: (page - 1) * limit,
      take: limit,
      orderBy: { id: "desc" },
    }),
    prisma.wishlist.count({ where: { userId } }),
  ]);
}

export function createWishlistItem(userId, productId) {
  return prisma.wishlist.create({
    data: { userId, productId: Number(productId) },
  });
}

export function deleteWishlistItem(userId, productId) {
  return prisma.wishlist.deleteMany({
    where: { userId, productId: Number(productId) },
  });
}
