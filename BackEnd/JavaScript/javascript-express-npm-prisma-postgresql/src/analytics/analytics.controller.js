import { getLowestEver, getUnitPrice } from "./analytics.service.js";
import { success } from "../utils/response.js";

export async function getLowestEverController(req, res) {
  const data = await getLowestEver(req.params.id);
  res.status(200).json(success(data));
}

export async function getUnitPriceController(req, res) {
  const data = await getUnitPrice(req.params.id);
  res.status(200).json(success(data));
}
