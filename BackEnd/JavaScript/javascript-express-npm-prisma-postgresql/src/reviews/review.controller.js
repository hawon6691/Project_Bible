import { createReview, getReviews } from "./review.service.js";
import { toReviewDto } from "./review.mapper.js";
import { success } from "../utils/response.js";

export async function getReviewsController(req, res) {
  const { items, meta } = await getReviews(req.params.productId);
  res.status(200).json(success(items.map(toReviewDto), meta));
}

export async function createReviewController(req, res) {
  const data = await createReview(req.user.id, req.params.productId, req.body);
  res.status(201).json(success(toReviewDto(data)));
}
