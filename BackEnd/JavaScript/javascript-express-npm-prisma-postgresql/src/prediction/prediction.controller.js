import { getPriceTrend } from "./prediction.service.js";
import { success } from "../utils/response.js";

export async function getPriceTrendController(req, res) {
  const data = await getPriceTrend(req.params.productId, req.query);
  res.status(200).json(success(data));
}
