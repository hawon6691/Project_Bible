import {
  addCompareItem,
  getCompareDetail,
  getCompareList,
  removeCompareItem,
} from "./compare.service.js";
import { success } from "../utils/response.js";

export async function addCompareItemController(req, res) {
  const data = await addCompareItem(req.headers["x-compare-key"], req.body);
  res.status(201).json(success(data));
}

export async function removeCompareItemController(req, res) {
  const data = await removeCompareItem(req.headers["x-compare-key"], req.params.productId);
  res.status(200).json(success(data));
}

export async function getCompareListController(req, res) {
  const data = await getCompareList(req.headers["x-compare-key"]);
  res.status(200).json(success(data));
}

export async function getCompareDetailController(req, res) {
  const data = await getCompareDetail(req.headers["x-compare-key"]);
  res.status(200).json(success(data));
}
