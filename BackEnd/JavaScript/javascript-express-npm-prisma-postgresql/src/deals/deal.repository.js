import { prisma } from "../prisma.js";

function dealInclude() {
  return {
    product: {
      select: {
        id: true,
        name: true,
        thumbnailUrl: true,
        price: true,
        discountPrice: true,
      },
    },
    dealProducts: {
      include: {
        product: {
          select: {
            id: true,
            name: true,
            thumbnailUrl: true,
            price: true,
            discountPrice: true,
          },
        },
      },
      orderBy: { id: "asc" },
    },
  };
}

export function findActiveDeals(now) {
  return prisma.deal.findMany({
    where: {
      isActive: true,
      startAt: { lte: now },
      endAt: { gte: now },
    },
    include: dealInclude(),
    orderBy: [{ startAt: "asc" }, { id: "asc" }],
  });
}

export function findDealById(id) {
  return prisma.deal.findUnique({
    where: { id: Number(id) },
    include: dealInclude(),
  });
}

export function createDeal(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('deals', 'id'), COALESCE((SELECT MAX(id) FROM deals), 0) + 1, false)",
    );
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('deal_products', 'id'), COALESCE((SELECT MAX(id) FROM deal_products), 0) + 1, false)",
    );

    return tx.deal.create({
      data,
      include: dealInclude(),
    });
  });
}

export function updateDeal(id, data) {
  return prisma.deal.update({
    where: { id: Number(id) },
    data,
    include: dealInclude(),
  });
}

export function deleteDealById(id) {
  return prisma.deal.delete({
    where: { id: Number(id) },
    include: dealInclude(),
  });
}
