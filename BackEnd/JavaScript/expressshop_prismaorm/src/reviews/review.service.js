import { createReview as createReviewRecord, findReviews } from "./review.repository.js";
import { badRequest } from "../utils/http-error.js";

export async function getReviews(productId) {
  const items = await findReviews(productId);
  return { items, meta: { total: items.length } };
}

export async function createReview(userId, productId, payload) {
  const { orderId, rating, content } = payload ?? {};
  if (!orderId || !rating || !content) throw badRequest("orderId, rating, content are required");
  return createReviewRecord({
    userId,
    productId: Number(productId),
    orderId: Number(orderId),
    rating: Number(rating),
    content,
  });
}
