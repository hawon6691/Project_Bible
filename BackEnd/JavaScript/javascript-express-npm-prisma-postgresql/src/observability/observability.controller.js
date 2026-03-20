import { success } from "../utils/response.js";
import { getMetricsSummary, getObservabilityDashboard, getRecentTraces } from "./observability.service.js";

export async function getObservabilityMetricsController(_req, res) {
  res.status(200).json(success(getMetricsSummary()));
}

export async function getObservabilityTracesController(req, res) {
  res.status(200).json(success({ items: getRecentTraces(req.query.limit, req.query.pathContains) }));
}

export async function getObservabilityDashboardController(_req, res) {
  const data = await getObservabilityDashboard();
  res.status(200).json(success(data));
}
