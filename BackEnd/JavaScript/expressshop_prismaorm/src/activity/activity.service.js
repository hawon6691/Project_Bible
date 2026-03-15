import {
  countRecentViews,
  countSearchHistories,
  deleteRecentViews,
  deleteSearchHistories,
  deleteSearchHistoryById,
  findRecentViews,
  findSearchHistories,
} from "./activity.repository.js";
import { notFound } from "../utils/http-error.js";

export async function getRecentViews(userId, query) {
  const page = Math.max(Number(query?.page ?? 1), 1);
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const [items, total] = await Promise.all([
    findRecentViews(userId, page, limit),
    countRecentViews(userId),
  ]);

  return {
    items,
    meta: { total, page, limit },
  };
}

export async function clearRecentViews(userId) {
  await deleteRecentViews(userId);
  return { message: "View history cleared" };
}

export async function getSearchHistories(userId) {
  const [items, total] = await Promise.all([
    findSearchHistories(userId),
    countSearchHistories(userId),
  ]);
  return { items, meta: { total } };
}

export async function clearSearchHistories(userId) {
  await deleteSearchHistories(userId);
  return { message: "Search history cleared" };
}

export async function deleteSearchHistory(userId, id) {
  const result = await deleteSearchHistoryById(userId, id);
  if (result.count === 0) {
    throw notFound("Search history not found");
  }
  return { message: "Search history deleted" };
}
