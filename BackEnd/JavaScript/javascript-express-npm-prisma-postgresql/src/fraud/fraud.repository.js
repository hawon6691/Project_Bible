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

export function findRealPriceEntries(productId, sellerId) {
  return prisma.priceEntry.findMany({
    where: {
      productId: Number(productId),
      isAvailable: true,
      ...(sellerId ? { sellerId: Number(sellerId) } : {}),
    },
    include: {
      seller: {
        select: {
          id: true,
          name: true,
        },
      },
    },
    orderBy: [{ price: "asc" }, { id: "asc" }],
  });
}

export function findFraudAlerts(status, page, limit) {
  const where = status ? { status } : {};
  return Promise.all([
    prisma.fraudAlert.findMany({
      where,
      include: {
        product: {
          select: {
            id: true,
            name: true,
          },
        },
        seller: {
          select: {
            id: true,
            name: true,
          },
        },
        priceEntry: {
          select: {
            id: true,
            price: true,
            shippingFee: true,
            shippingType: true,
            isAvailable: true,
          },
        },
      },
      orderBy: [{ createdAt: "desc" }, { id: "desc" }],
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.fraudAlert.count({ where }),
  ]);
}

export function findFraudAlertById(alertId) {
  return prisma.fraudAlert.findUnique({
    where: { id: Number(alertId) },
    include: {
      product: {
        select: {
          id: true,
          name: true,
        },
      },
      seller: {
        select: {
          id: true,
          name: true,
        },
      },
    },
  });
}

export function updateFraudAlert(alertId, data) {
  return prisma.fraudAlert.update({
    where: { id: Number(alertId) },
    data,
  });
}

export function updatePriceEntryAvailability(priceEntryId, isAvailable) {
  return prisma.priceEntry.update({
    where: { id: Number(priceEntryId) },
    data: { isAvailable },
  });
}
