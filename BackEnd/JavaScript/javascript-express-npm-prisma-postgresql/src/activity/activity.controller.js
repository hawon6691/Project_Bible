import {
  clearRecentViews,
  clearSearchHistories,
  deleteSearchHistory,
  getRecentViews,
  getSearchHistories,
} from "./activity.service.js";
import {
  toRecentViewDto,
  toSearchHistoryDto,
} from "./activity.mapper.js";
import { success } from "../utils/response.js";

export async function getRecentViewsController(req, res) {
  const { items, meta } = await getRecentViews(req.user.id, req.query);
  res.status(200).json(success(items.map(toRecentViewDto), meta));
}

export async function clearRecentViewsController(req, res) {
  const data = await clearRecentViews(req.user.id);
  res.status(200).json(success(data));
}

export async function getSearchHistoriesController(req, res) {
  const { items, meta } = await getSearchHistories(req.user.id);
  res.status(200).json(success(items.map(toSearchHistoryDto), meta));
}

export async function clearSearchHistoriesController(req, res) {
  const data = await clearSearchHistories(req.user.id);
  res.status(200).json(success(data));
}

export async function deleteSearchHistoryController(req, res) {
  const data = await deleteSearchHistory(req.user.id, req.params.id);
  res.status(200).json(success(data));
}
