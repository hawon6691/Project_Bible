import {
  createAdminDeal,
  deleteAdminDeal,
  getDeal,
  getDeals,
  updateAdminDeal,
} from "./deal.service.js";
import { toDealDetailDto, toDealDto } from "./deal.mapper.js";
import { success } from "../utils/response.js";

export async function getDealsController(_req, res) {
  const { items, meta } = await getDeals();
  res.status(200).json(success(items.map(toDealDto), meta));
}

export async function getDealController(req, res) {
  const data = await getDeal(req.params.id);
  res.status(200).json(success(toDealDetailDto(data)));
}

export async function createDealController(req, res) {
  const data = await createAdminDeal(req.body);
  res.status(201).json(success(toDealDetailDto(data)));
}

export async function updateDealController(req, res) {
  const data = await updateAdminDeal(req.params.id, req.body);
  res.status(200).json(success(toDealDetailDto(data)));
}

export async function deleteDealController(req, res) {
  const data = await deleteAdminDeal(req.params.id);
  res.status(200).json(success(data));
}
