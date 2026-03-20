import {
  estimatePcBuildUsedPrice,
  getCategoryUsedPrices,
  getProductUsedPrice,
} from "./used-market.service.js";
import { success } from "../utils/response.js";

export async function getProductUsedPriceController(req, res) {
  const data = await getProductUsedPrice(req.params.id);
  res.status(200).json(success(data));
}

export async function getCategoryUsedPricesController(req, res) {
  const { items, meta } = await getCategoryUsedPrices(req.params.id, req.query);
  res.status(200).json(success(items, meta));
}

export async function estimatePcBuildUsedPriceController(req, res) {
  const data = await estimatePcBuildUsedPrice(req.user.id, req.params.buildId);
  res.status(201).json(success(data));
}
