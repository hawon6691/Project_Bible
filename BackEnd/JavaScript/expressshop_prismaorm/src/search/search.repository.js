import { prisma } from "../prisma.js";

function bumpSequence(tx, tableName) {
  return tx.$executeRawUnsafe(
    `SELECT setval(pg_get_serial_sequence('${tableName}', 'id'), COALESCE((SELECT MAX(id) FROM ${tableName}), 0) + 1, false)`,
  );
}

export function findSearchProducts(keyword, categoryId) {
  const trimmed = String(keyword ?? "").trim();

  return prisma.product.findMany({
    where: {
      deletedAt: null,
      status: { not: "HIDDEN" },
      ...(categoryId ? { categoryId: Number(categoryId) } : {}),
      ...(trimmed
        ? {
            OR: [
              { name: { contains: trimmed, mode: "insensitive" } },
              { description: { contains: trimmed, mode: "insensitive" } },
              { category: { name: { contains: trimmed, mode: "insensitive" } } },
            ],
          }
        : {}),
    },
    select: {
      id: true,
      name: true,
      description: true,
      price: true,
      lowestPrice: true,
      thumbnailUrl: true,
      popularityScore: true,
      averageRating: true,
      reviewCount: true,
      sellerCount: true,
      createdAt: true,
      viewCount: true,
      category: {
        select: {
          id: true,
          name: true,
          parentId: true,
        },
      },
      specs: {
        select: {
          value: true,
          numericValue: true,
          specDefinition: {
            select: {
              id: true,
              name: true,
              unit: true,
            },
          },
        },
      },
    },
  });
}

export function findAutocompleteProducts(query, limit) {
  const trimmed = String(query ?? "").trim();

  return prisma.product.findMany({
    where: {
      deletedAt: null,
      status: { not: "HIDDEN" },
      OR: [
        { name: { contains: trimmed, mode: "insensitive" } },
        { description: { contains: trimmed, mode: "insensitive" } },
      ],
    },
    select: {
      id: true,
      name: true,
      thumbnailUrl: true,
      lowestPrice: true,
    },
    orderBy: [{ popularityScore: "desc" }, { id: "desc" }],
    take: limit,
  });
}

export function findAutocompleteCategories(query, limit) {
  const trimmed = String(query ?? "").trim();

  return prisma.category.findMany({
    where: {
      name: { contains: trimmed, mode: "insensitive" },
    },
    select: {
      id: true,
      name: true,
      parent: {
        select: {
          id: true,
          name: true,
        },
      },
    },
    orderBy: [{ parentId: "asc" }, { id: "asc" }],
    take: limit,
  });
}

export function findActiveSearchSynonyms(query, limit) {
  const trimmed = String(query ?? "").trim();

  return prisma.searchSynonym.findMany({
    where: {
      isActive: true,
      word: { contains: trimmed, mode: "insensitive" },
    },
    orderBy: { id: "asc" },
    take: limit,
  });
}

export function findPopularKeywordsMatching(query, limit) {
  const trimmed = String(query ?? "").trim();

  return prisma.searchLog.groupBy({
    by: ["keyword"],
    where: {
      keyword: { startsWith: trimmed, mode: "insensitive" },
    },
    _count: {
      keyword: true,
    },
    orderBy: {
      _count: {
        keyword: "desc",
      },
    },
    take: limit,
  });
}

export function createSearchLog(data) {
  return prisma.$transaction(async (tx) => {
    await bumpSequence(tx, "search_logs");
    return tx.searchLog.create({ data });
  });
}

export function findPopularSearchLogs(limit) {
  return prisma.searchLog.groupBy({
    by: ["keyword"],
    _count: {
      keyword: true,
    },
    orderBy: {
      _count: {
        keyword: "desc",
      },
    },
    take: limit,
  });
}

export function findRecentSearchKeywordByUser(userId, keyword) {
  return prisma.searchRecentKeyword.findFirst({
    where: {
      userId: Number(userId),
      keyword,
    },
  });
}

export function createRecentSearchKeyword(data) {
  return prisma.$transaction(async (tx) => {
    await bumpSequence(tx, "search_recent_keywords");
    return tx.searchRecentKeyword.create({ data });
  });
}

export function updateRecentSearchKeyword(id, data) {
  return prisma.searchRecentKeyword.update({
    where: { id: Number(id) },
    data,
  });
}

export function findRecentSearchKeywords(userId, limit) {
  return prisma.searchRecentKeyword.findMany({
    where: { userId: Number(userId) },
    orderBy: [{ searchedAt: "desc" }, { id: "desc" }],
    take: limit,
  });
}

export function deleteOverflowRecentSearchKeywords(userId, keepIds) {
  return prisma.searchRecentKeyword.deleteMany({
    where: {
      userId: Number(userId),
      ...(keepIds.length > 0 ? { id: { notIn: keepIds.map((item) => Number(item)) } } : {}),
    },
  });
}

export function findRecentSearchKeywordById(userId, id) {
  return prisma.searchRecentKeyword.findFirst({
    where: {
      id: Number(id),
      userId: Number(userId),
    },
  });
}

export function deleteRecentSearchKeyword(id) {
  return prisma.searchRecentKeyword.delete({
    where: { id: Number(id) },
  });
}

export function clearRecentSearchKeywords(userId) {
  return prisma.searchRecentKeyword.deleteMany({
    where: { userId: Number(userId) },
  });
}

export function findSearchWeightSettings() {
  return prisma.searchWeightSetting.findMany({
    orderBy: { id: "asc" },
  });
}

export function upsertSearchWeightSetting(field, weight) {
  return prisma.$transaction(async (tx) => {
    await bumpSequence(tx, "search_weight_settings");
    return tx.searchWeightSetting.upsert({
      where: { field },
      update: { weight },
      create: { field, weight },
    });
  });
}

export function countProductQueryViews() {
  return prisma.productQueryView.count();
}

export function countSearchIndexOutboxByStatus(status) {
  return prisma.searchIndexOutbox.count({
    where: { status },
  });
}

export function countSearchIndexOutbox() {
  return prisma.searchIndexOutbox.count();
}

export function findFailedSearchIndexOutbox(limit) {
  return prisma.searchIndexOutbox.findMany({
    where: { status: "FAILED" },
    orderBy: { id: "asc" },
    take: limit,
  });
}

export function requeueFailedSearchIndexOutbox(ids) {
  return prisma.searchIndexOutbox.updateMany({
    where: {
      id: { in: ids.map((item) => Number(item)) },
      status: "FAILED",
    },
    data: {
      status: "PENDING",
      lastError: null,
      processedAt: null,
      attemptCount: { increment: 1 },
    },
  });
}

export function findProductForSearchIndex(productId) {
  return prisma.product.findFirst({
    where: {
      id: Number(productId),
      deletedAt: null,
    },
    select: {
      id: true,
      name: true,
      categoryId: true,
      thumbnailUrl: true,
      status: true,
      price: true,
      lowestPrice: true,
      sellerCount: true,
      averageRating: true,
      reviewCount: true,
      viewCount: true,
      popularityScore: true,
    },
  });
}

export function findProductsForSearchIndex() {
  return prisma.product.findMany({
    where: {
      deletedAt: null,
    },
    select: {
      id: true,
      name: true,
      categoryId: true,
      thumbnailUrl: true,
      status: true,
      price: true,
      lowestPrice: true,
      sellerCount: true,
      averageRating: true,
      reviewCount: true,
      viewCount: true,
      popularityScore: true,
    },
    orderBy: { id: "asc" },
  });
}

export function upsertProductQueryView(product) {
  return prisma.$transaction(async (tx) => {
    await bumpSequence(tx, "product_query_views");
    return tx.productQueryView.upsert({
      where: { productId: Number(product.productId) },
      update: product,
      create: product,
    });
  });
}

export function createSearchIndexOutbox(data) {
  return prisma.$transaction(async (tx) => {
    await bumpSequence(tx, "search_index_outbox");
    return tx.searchIndexOutbox.create({ data });
  });
}
