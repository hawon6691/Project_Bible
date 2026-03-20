import {
  approveFraudAlert,
  getFraudAlerts,
  getRealPrice,
  rejectFraudAlert,
} from "./fraud.service.js";
import { success } from "../utils/response.js";

export async function getRealPriceController(req, res) {
  const data = await getRealPrice(req.params.id, req.query);
  res.status(200).json(success(data));
}

export async function getFraudAlertsController(req, res) {
  const { items, meta } = await getFraudAlerts(req.query);
  res.status(200).json(success(items, meta));
}

export async function approveFraudAlertController(req, res) {
  const data = await approveFraudAlert(req.params.id, req.user.id);
  res.status(200).json(success(data));
}

export async function rejectFraudAlertController(req, res) {
  const data = await rejectFraudAlert(req.params.id, req.user.id);
  res.status(200).json(success(data));
}
