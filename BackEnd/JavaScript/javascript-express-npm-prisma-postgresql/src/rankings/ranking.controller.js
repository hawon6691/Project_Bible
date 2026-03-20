import {
  getPopularProducts,
  getPopularSearches,
  recalculateRankings,
} from "./ranking.service.js";
import { toRankedProductDto, toSearchRankDto } from "./ranking.mapper.js";
import { success } from "../utils/response.js";

export async function getPopularProductsController(req, res) {
  const { items, meta } = await getPopularProducts(req.query);
  res.status(200).json(success(items.map(toRankedProductDto), meta));
}

export async function getPopularSearchesController(req, res) {
  const { items, meta } = await getPopularSearches(req.query);
  res.status(200).json(success(items.map(toSearchRankDto), meta));
}

export async function recalculateRankingsController(_req, res) {
  const data = await recalculateRankings();
  res.status(200).json(success(data));
}
