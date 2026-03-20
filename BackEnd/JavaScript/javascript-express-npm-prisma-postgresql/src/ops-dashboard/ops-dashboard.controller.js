import { success } from "../utils/response.js";
import { getOpsDashboardSummary } from "./ops-dashboard.service.js";

export async function getOpsDashboardSummaryController(_req, res) {
  const data = await getOpsDashboardSummary();
  res.status(200).json(success(data));
}
