import {
  countNews,
  createNews,
  createNewsCategory,
  deleteNewsById,
  deleteNewsCategoryById,
  findNews,
  findNewsById,
  findNewsCategories,
  findNewsCategoryById,
  updateNews,
} from "./news.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getNewsList(query) {
  const items = await findNews(query);
  const total = await countNews(query);
  return { items, meta: { total, limit: Math.min(Math.max(Number(query?.limit ?? 20), 1), 100) } };
}

export async function getNewsCategories() {
  const items = await findNewsCategories();
  return { items, meta: { total: items.length } };
}

export async function getNewsDetail(id) {
  const item = await findNewsById(id);
  if (!item) throw notFound("News not found");
  return item;
}

export async function createNewsItem(payload) {
  if (!payload?.title || !payload?.content || !payload?.categoryId) {
    throw badRequest("title, content, categoryId are required");
  }
  const productIds = Array.isArray(payload?.productIds) ? payload.productIds.map(Number).filter(Boolean) : [];
  return createNews({
    title: payload.title,
    content: payload.content,
    categoryId: Number(payload.categoryId),
    thumbnailUrl: payload?.thumbnailUrl ?? null,
    products: productIds.length > 0 ? { create: productIds.map((productId) => ({ productId })) } : undefined,
  });
}

export async function updateNewsItem(id, payload) {
  const existing = await findNewsById(id);
  if (!existing) throw notFound("News not found");
  return updateNews(id, {
    title: payload?.title ?? undefined,
    content: payload?.content ?? undefined,
    categoryId: payload?.categoryId ? Number(payload.categoryId) : undefined,
    thumbnailUrl: payload?.thumbnailUrl ?? undefined,
  });
}

export async function deleteNewsItem(id) {
  const existing = await findNewsById(id);
  if (!existing) throw notFound("News not found");
  await deleteNewsById(id);
  return { message: "News deleted" };
}

export async function createNewsCategoryItem(payload) {
  if (!payload?.name || !payload?.slug) throw badRequest("name and slug are required");
  return createNewsCategory({ name: payload.name, slug: payload.slug });
}

export async function deleteNewsCategoryItem(id) {
  const existing = await findNewsCategoryById(id);
  if (!existing) throw notFound("News category not found");
  await deleteNewsCategoryById(id);
  return { message: "News category deleted" };
}
