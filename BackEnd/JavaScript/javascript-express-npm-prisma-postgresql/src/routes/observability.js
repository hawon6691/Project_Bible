import { Router } from "express";

import {
  getObservabilityDashboardController,
  getObservabilityMetricsController,
  getObservabilityTracesController,
} from "../observability/observability.controller.js";
import { validateObservabilityTracesQuery } from "../observability/observability.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get(
  "/admin/observability/metrics",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getObservabilityMetricsController),
);
router.get(
  "/admin/observability/traces",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateObservabilityTracesQuery),
  asyncHandler(getObservabilityTracesController),
);
router.get(
  "/admin/observability/dashboard",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getObservabilityDashboardController),
);

export default router;
