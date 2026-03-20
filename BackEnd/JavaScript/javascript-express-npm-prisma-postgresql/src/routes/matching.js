import { Router } from "express";
import {
  approveMappingController,
  autoMatchController,
  getMatchingStatsController,
  getPendingMappingsController,
  rejectMappingController,
} from "../matching/matching.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateApproveMapping, validateRejectMapping } from "../matching/matching.validator.js";

const router = Router();

router.get("/matching/pending", requireAuth, requireRole("ADMIN"), asyncHandler(getPendingMappingsController));
router.patch("/matching/:id/approve", requireAuth, requireRole("ADMIN"), validate(validateApproveMapping), asyncHandler(approveMappingController));
router.patch("/matching/:id/reject", requireAuth, requireRole("ADMIN"), validate(validateRejectMapping), asyncHandler(rejectMappingController));
router.post("/matching/auto-match", requireAuth, requireRole("ADMIN"), asyncHandler(autoMatchController));
router.get("/matching/stats", requireAuth, requireRole("ADMIN"), asyncHandler(getMatchingStatsController));

export default router;
