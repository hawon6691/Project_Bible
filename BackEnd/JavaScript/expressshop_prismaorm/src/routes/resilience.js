import { Router } from "express";

import {
  getCircuitBreakerPoliciesController,
  getCircuitBreakerSnapshotController,
  getCircuitBreakerSnapshotsController,
  resetCircuitBreakerController,
} from "../resilience/resilience.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get(
  "/resilience/circuit-breakers",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getCircuitBreakerSnapshotsController),
);
router.get(
  "/resilience/circuit-breakers/policies",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getCircuitBreakerPoliciesController),
);
router.get(
  "/resilience/circuit-breakers/:name",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getCircuitBreakerSnapshotController),
);
router.post(
  "/resilience/circuit-breakers/:name/reset",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(resetCircuitBreakerController),
);

export default router;
