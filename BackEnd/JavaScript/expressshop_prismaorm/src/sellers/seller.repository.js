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
