import { prisma } from "../prisma.js";

export function findProducts(where, page, limit) {
  return Promise.all([
    prisma.product.findMany({
      where,
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
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy: { id: "asc" },
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.product.count({ where }),
  ]);
}

export function findProductById(productId) {
  return prisma.product.findUnique({
    where: { id: Number(productId) },
    include: {
      category: {
        select: { id: true, name: true, parentId: true },
      },
      options: {
        select: { id: true, name: true, values: true, createdAt: true, updatedAt: true },
        orderBy: { id: "asc" },
      },
      images: {
        select: { id: true, url: true, isMain: true, sortOrder: true, createdAt: true },
        orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
      },
      specs: {
        select: {
          id: true,
          value: true,
          numericValue: true,
          specDefinition: {
            select: { id: true, name: true, unit: true, dataType: true },
          },
        },
        orderBy: { id: "asc" },
      },
    },
  });
}

export function findSpecDefinitions(categoryId) {
  return prisma.specDefinition.findMany({
    where: categoryId ? { categoryId: Number(categoryId) } : {},
    orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
  });
}

export function findProductSpecs(productId) {
  return prisma.productSpec.findMany({
    where: { productId: Number(productId) },
    include: {
      specDefinition: {
        select: {
          id: true,
          name: true,
          unit: true,
          dataType: true,
          isComparable: true,
        },
      },
    },
    orderBy: { id: "asc" },
  });
}

export function findProductPrices(productId) {
  return prisma.priceEntry.findMany({
    where: { productId: Number(productId) },
    include: {
      seller: {
        select: {
          id: true,
          name: true,
          trustScore: true,
          trustGrade: true,
          isActive: true,
        },
      },
    },
    orderBy: [{ price: "asc" }, { id: "asc" }],
  });
}

export function findPriceHistory(productId) {
  return prisma.priceHistory.findMany({
    where: { productId: Number(productId) },
    orderBy: { date: "desc" },
  });
}
