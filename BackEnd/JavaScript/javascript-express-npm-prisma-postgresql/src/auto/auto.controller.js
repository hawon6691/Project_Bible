import { getCarModels, getLeaseOffers } from "./auto.service.js";
import { success } from "../utils/response.js";

export async function getCarModelsController(req, res) {
  const data = await getCarModels(req.query);
  res.status(200).json(success(data));
}

export async function getLeaseOffersController(req, res) {
  const data = await getLeaseOffers(req.params.id);
  res.status(200).json(success(data));
}
