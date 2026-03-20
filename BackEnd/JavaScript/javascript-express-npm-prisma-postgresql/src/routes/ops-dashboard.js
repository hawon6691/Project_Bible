import { Router } from "express";

import { getOpsDashboardSummaryController } from "../ops-dashboard/ops-dashboard.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get(
  "/admin/ops-dashboard/summary",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getOpsDashboardSummaryController),
);

export default router;
