import { getApiHealth, getDocsStatus, getHealth } from "./health.service.js";
import { success } from "../utils/response.js";

export async function healthController(_req, res) {
  const data = await getHealth();
  res.status(200).json(success(data));
}

export async function apiHealthController(_req, res) {
  const data = await getApiHealth();
  res.status(200).json(success(data));
}

export function docsStatusController(_req, res) {
  res.status(200).json(success(getDocsStatus()));
}
