import {
  createAdminRecommendation,
  deleteAdminRecommendation,
  getAdminRecommendations,
  getPersonalizedRecommendations,
  getTodayRecommendations,
} from "./recommendation.service.js";
import { toRecommendationDto } from "./recommendation.mapper.js";
import { toProductSummaryDto } from "../products/product.mapper.js";
import { success } from "../utils/response.js";

export async function getTodayRecommendationsController(_req, res) {
  const { items, meta } = await getTodayRecommendations();
  res.status(200).json(success(items.map(toProductSummaryDto), meta));
}

export async function getPersonalizedRecommendationsController(req, res) {
  const { items, meta } = await getPersonalizedRecommendations(req.user.id, req.query);
  res.status(200).json(success(items.map(toProductSummaryDto), meta));
}

export async function getAdminRecommendationsController(_req, res) {
  const { items, meta } = await getAdminRecommendations();
  res.status(200).json(success(items.map(toRecommendationDto), meta));
}

export async function createAdminRecommendationController(req, res) {
  const data = await createAdminRecommendation(req.body);
  res.status(201).json(success(toRecommendationDto(data)));
}

export async function deleteAdminRecommendationController(req, res) {
  const data = await deleteAdminRecommendation(req.params.id);
  res.status(200).json(success(data));
}
