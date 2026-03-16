import { prisma } from "../prisma.js";

export function findProducts(where, page, limit, orderBy) {
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
        createdAt: true,
        category: {
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy,
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.product.count({ where }),
  ]);
}

export function findProductById(productId) {
  return prisma.product.findFirst({
    where: { id: Number(productId), deletedAt: null },
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
            select: {
              id: true,
              name: true,
              unit: true,
              dataType: true,
              isComparable: true,
              sortOrder: true,
            },
          },
        },
        orderBy: { id: "asc" },
      },
      priceEntries: {
        where: { isAvailable: true },
        include: {
          seller: {
            select: {
              id: true,
              name: true,
              logoUrl: true,
              trustScore: true,
              trustGrade: true,
              isActive: true,
            },
          },
        },
        orderBy: [{ price: "asc" }, { id: "asc" }],
      },
    },
  });
}

export function findProductByIds(productIds) {
  return prisma.product.findMany({
    where: {
      id: { in: productIds.map((item) => Number(item)) },
      deletedAt: null,
    },
    select: {
      id: true,
      name: true,
      thumbnailUrl: true,
      lowestPrice: true,
      specs: {
        select: {
          id: true,
          value: true,
          numericValue: true,
          specDefinitionId: true,
          specDefinition: {
            select: {
              id: true,
              name: true,
              unit: true,
              sortOrder: true,
              isComparable: true,
            },
          },
        },
      },
    },
    orderBy: { id: "asc" },
  });
}

export function createProduct(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('products', 'id'), COALESCE((SELECT MAX(id) FROM products), 0) + 1, false)",
    );
    return tx.product.create({
      data,
    });
  });
}

export function updateProduct(productId, data) {
  return prisma.product.update({
    where: { id: Number(productId) },
    data,
  });
}

export function softDeleteProduct(productId) {
  return prisma.product.update({
    where: { id: Number(productId) },
    data: { deletedAt: new Date() },
  });
}

export function findProductOptionById(optionId) {
  return prisma.productOption.findUnique({
    where: { id: Number(optionId) },
  });
}

export function createProductOption(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('product_options', 'id'), COALESCE((SELECT MAX(id) FROM product_options), 0) + 1, false)",
    );
    return tx.productOption.create({
      data,
    });
  });
}

export function updateProductOption(optionId, data) {
  return prisma.productOption.update({
    where: { id: Number(optionId) },
    data,
  });
}

export function deleteProductOption(optionId) {
  return prisma.productOption.delete({
    where: { id: Number(optionId) },
  });
}

export function findSpecDefinitions(categoryId) {
  return prisma.specDefinition.findMany({
    where: categoryId ? { categoryId: Number(categoryId) } : {},
    orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
  });
}

export function findSpecDefinitionById(specDefinitionId) {
  return prisma.specDefinition.findUnique({
    where: { id: Number(specDefinitionId) },
  });
}

export function createSpecDefinition(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('spec_definitions', 'id'), COALESCE((SELECT MAX(id) FROM spec_definitions), 0) + 1, false)",
    );
    return tx.specDefinition.create({
      data,
    });
  });
}

export function updateSpecDefinition(specDefinitionId, data) {
  return prisma.specDefinition.update({
    where: { id: Number(specDefinitionId) },
    data,
  });
}

export function deleteSpecDefinition(specDefinitionId) {
  return prisma.specDefinition.delete({
    where: { id: Number(specDefinitionId) },
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

export async function replaceProductSpecs(productId, specs) {
  await prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('product_specs', 'id'), COALESCE((SELECT MAX(id) FROM product_specs), 0) + 1, false)",
    );
    await tx.productSpec.deleteMany({
      where: { productId: Number(productId) },
    });

    if (specs.length > 0) {
      await tx.productSpec.createMany({
        data: specs.map((item) => ({
          productId: Number(productId),
          specDefinitionId: Number(item.specDefinitionId),
          value: item.value,
          numericValue: item.numericValue ?? null,
        })),
      });
    }
  });

  return findProductSpecs(productId);
}

export function findSpecScoresByDefinitionIds(specDefinitionIds) {
  return prisma.specScore.findMany({
    where: {
      specDefinitionId: {
        in: specDefinitionIds.map((item) => Number(item)),
      },
    },
    orderBy: [{ specDefinitionId: "asc" }, { score: "desc" }, { id: "asc" }],
  });
}

export async function replaceSpecScores(specDefinitionId, scores) {
  await prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('spec_scores', 'id'), COALESCE((SELECT MAX(id) FROM spec_scores), 0) + 1, false)",
    );
    await tx.specScore.deleteMany({
      where: { specDefinitionId: Number(specDefinitionId) },
    });

    if (scores.length > 0) {
      await tx.specScore.createMany({
        data: scores.map((item) => ({
          specDefinitionId: Number(specDefinitionId),
          value: item.value,
          score: Number(item.score),
          benchmarkSource: item.benchmarkSource ?? null,
        })),
      });
    }
  });

  return prisma.specScore.findMany({
    where: { specDefinitionId: Number(specDefinitionId) },
    orderBy: [{ score: "desc" }, { id: "asc" }],
  });
}

export function findProductPrices(productId) {
  return prisma.priceEntry.findMany({
    where: { productId: Number(productId), isAvailable: true },
    include: {
      seller: {
        select: {
          id: true,
          name: true,
          logoUrl: true,
          trustScore: true,
          trustGrade: true,
          isActive: true,
        },
      },
    },
    orderBy: [{ price: "asc" }, { id: "asc" }],
  });
}

export function findPriceEntryById(priceEntryId) {
  return prisma.priceEntry.findUnique({
    where: { id: Number(priceEntryId) },
    include: {
      seller: {
        select: {
          id: true,
          name: true,
          logoUrl: true,
          trustScore: true,
          trustGrade: true,
          isActive: true,
        },
      },
      product: {
        select: {
          id: true,
          name: true,
          lowestPrice: true,
        },
      },
    },
  });
}

export function createPriceEntry(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('price_entries', 'id'), COALESCE((SELECT MAX(id) FROM price_entries), 0) + 1, false)",
    );
    return tx.priceEntry.create({
      data,
    });
  });
}

export function updatePriceEntry(priceEntryId, data) {
  return prisma.priceEntry.update({
    where: { id: Number(priceEntryId) },
    data,
  });
}

export function deletePriceEntry(priceEntryId) {
  return prisma.priceEntry.delete({
    where: { id: Number(priceEntryId) },
  });
}

export async function recalculateProductPricing(productId) {
  const [aggregate, sellerCount] = await Promise.all([
    prisma.priceEntry.aggregate({
      where: {
        productId: Number(productId),
        isAvailable: true,
      },
      _min: { price: true },
      _avg: { price: true },
      _max: { price: true },
    }),
    prisma.priceEntry.count({
      where: {
        productId: Number(productId),
        isAvailable: true,
      },
    }),
  ]);

  return prisma.product.update({
    where: { id: Number(productId) },
    data: {
      lowestPrice: aggregate._min.price ?? null,
      sellerCount,
    },
  });
}

export function findPriceHistory(productId) {
  return prisma.priceHistory.findMany({
    where: { productId: Number(productId) },
    orderBy: { date: "desc" },
  });
}

export function findPriceAlertsByUser(userId) {
  return prisma.priceAlert.findMany({
    where: {
      userId: Number(userId),
      isActive: true,
    },
    include: {
      product: {
        select: {
          id: true,
          name: true,
          lowestPrice: true,
        },
      },
    },
    orderBy: { id: "desc" },
  });
}

export function findPriceAlertById(priceAlertId) {
  return prisma.priceAlert.findUnique({
    where: { id: Number(priceAlertId) },
    include: {
      product: {
        select: {
          id: true,
          name: true,
          lowestPrice: true,
        },
      },
    },
  });
}

export function findPriceAlertByUserAndProduct(userId, productId) {
  return prisma.priceAlert.findFirst({
    where: {
      userId: Number(userId),
      productId: Number(productId),
      isActive: true,
    },
  });
}

export function createPriceAlert(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('price_alerts', 'id'), COALESCE((SELECT MAX(id) FROM price_alerts), 0) + 1, false)",
    );
    return tx.priceAlert.create({
      data,
      include: {
        product: {
          select: {
            id: true,
            name: true,
            lowestPrice: true,
          },
        },
      },
    });
  });
}

export function deletePriceAlert(priceAlertId) {
  return prisma.priceAlert.update({
    where: { id: Number(priceAlertId) },
    data: { isActive: false },
    include: {
      product: {
        select: {
          id: true,
          name: true,
          lowestPrice: true,
        },
      },
    },
  });
}
