import { prisma } from "../prisma.js";

export function findCategoryById(categoryId) {
  return prisma.category.findUnique({
    where: { id: Number(categoryId) },
    select: {
      id: true,
      name: true,
    },
  });
}

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
      categoryId: true,
    },
  });
}

export function findUsedPricesByProduct(productId, limit = 2) {
  return prisma.usedPrice.findMany({
    where: { productId: Number(productId) },
    orderBy: [{ collectedAt: "desc" }, { id: "desc" }],
    take: limit,
  });
}

export function findCategoryProducts(categoryId, page, limit) {
  const where = {
    categoryId: Number(categoryId),
    deletedAt: null,
  };

  return Promise.all([
    prisma.product.findMany({
      where,
      select: {
        id: true,
        name: true,
        price: true,
        discountPrice: true,
        lowestPrice: true,
      },
      orderBy: [{ popularityScore: "desc" }, { salesCount: "desc" }, { id: "asc" }],
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.product.count({ where }),
  ]);
}

export function findUsedPricesByProductIds(productIds) {
  return prisma.usedPrice.findMany({
    where: {
      productId: {
        in: productIds.map((item) => Number(item)),
      },
    },
    orderBy: [{ productId: "asc" }, { collectedAt: "desc" }, { id: "desc" }],
  });
}

export function findPcBuildById(buildId) {
  return prisma.pcBuild.findFirst({
    where: {
      id: Number(buildId),
      deletedAt: null,
    },
    select: {
      id: true,
      userId: true,
      name: true,
      purpose: true,
      totalPrice: true,
    },
  });
}

export function findBuildParts(buildId) {
  return prisma.buildPart.findMany({
    where: { buildId: Number(buildId) },
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
    orderBy: { id: "asc" },
  });
}
