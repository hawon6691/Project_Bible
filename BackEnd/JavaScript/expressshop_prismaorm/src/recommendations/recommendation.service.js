import {
  createRecommendation,
  deleteRecommendationById,
  findAdminRecommendations,
  findPersonalizedProducts,
  findRecommendationById,
  findRecentViewCategories,
  findTodayRecommendations,
} from "./recommendation.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getTodayRecommendations() {
  const items = await findTodayRecommendations(new Date());
  return { items: items.map((item) => item.product), meta: { total: items.length } };
}

export async function getPersonalizedRecommendations(userId, query) {
  const limit = Math.min(Math.max(Number(query?.limit ?? 10), 1), 50);
  const recentViews = await findRecentViewCategories(userId);
  const categoryIds = [...new Set(recentViews.map((item) => item.product.categoryId))];
  const items = await findPersonalizedProducts(categoryIds, limit);
  return { items, meta: { total: items.length, limit } };
}

export async function getAdminRecommendations() {
  const items = await findAdminRecommendations();
  return { items, meta: { total: items.length } };
}

export async function createAdminRecommendation(payload) {
  const productId = Number(payload?.productId);
  if (!productId || !payload?.type) {
    throw badRequest("productId and type are required");
  }

  return createRecommendation({
    productId,
    type: payload.type,
    sortOrder: Number(payload?.sortOrder ?? 0),
    startDate: payload?.startDate ? new Date(payload.startDate) : null,
    endDate: payload?.endDate ? new Date(payload.endDate) : null,
  });
}

export async function deleteAdminRecommendation(id) {
  const existing = await findRecommendationById(id);
  if (!existing) {
    throw notFound("Recommendation not found");
  }
  await deleteRecommendationById(id);
  return { message: "Recommendation deleted" };
}
