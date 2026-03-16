import { prisma } from "../prisma.js";

function newsInclude() {
  return {
    category: true,
    products: {
      include: {
        product: {
          select: { id: true, name: true, thumbnailUrl: true, lowestPrice: true },
        },
      },
      orderBy: { id: "asc" },
    },
  };
}

export function findNews(query) {
  return prisma.news.findMany({
    where: {
      ...(query?.category ? { category: { slug: String(query.category) } } : {}),
    },
    include: newsInclude(),
    orderBy: { createdAt: "desc" },
    take: Math.min(Math.max(Number(query?.limit ?? 20), 1), 100),
  });
}

export function countNews(query) {
  return prisma.news.count({
    where: {
      ...(query?.category ? { category: { slug: String(query.category) } } : {}),
    },
  });
}

export function findNewsCategories() {
  return prisma.newsCategory.findMany({ orderBy: { id: "asc" } });
}

export function findNewsById(id) {
  return prisma.news.findUnique({ where: { id: Number(id) }, include: newsInclude() });
}

export function createNews(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe("SELECT setval(pg_get_serial_sequence('news', 'id'), COALESCE((SELECT MAX(id) FROM news), 0) + 1, false)");
    await tx.$executeRawUnsafe("SELECT setval(pg_get_serial_sequence('news_products', 'id'), COALESCE((SELECT MAX(id) FROM news_products), 0) + 1, false)");
    return tx.news.create({ data, include: newsInclude() });
  });
}

export function updateNews(id, data) {
  return prisma.news.update({ where: { id: Number(id) }, data, include: newsInclude() });
}

export function deleteNewsById(id) {
  return prisma.news.delete({ where: { id: Number(id) }, include: newsInclude() });
}

export function createNewsCategory(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe("SELECT setval(pg_get_serial_sequence('news_categories', 'id'), COALESCE((SELECT MAX(id) FROM news_categories), 0) + 1, false)");
    return tx.newsCategory.create({ data });
  });
}

export function deleteNewsCategoryById(id) {
  return prisma.newsCategory.delete({ where: { id: Number(id) } });
}

export function findNewsCategoryById(id) {
  return prisma.newsCategory.findUnique({ where: { id: Number(id) } });
}
