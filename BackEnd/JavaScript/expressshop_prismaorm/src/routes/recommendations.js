import { Router } from "express";

import {
  createAdminRecommendationController,
  deleteAdminRecommendationController,
  getAdminRecommendationsController,
  getPersonalizedRecommendationsController,
  getTodayRecommendationsController,
} from "../recommendations/recommendation.controller.js";
import { validateCreateRecommendation } from "../recommendations/recommendation.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/recommendations/today", asyncHandler(getTodayRecommendationsController));
router.get(
  "/recommendations/personalized",
  requireAuth,
  asyncHandler(getPersonalizedRecommendationsController),
);
router.get(
  "/admin/recommendations",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getAdminRecommendationsController),
);
router.post(
  "/admin/recommendations",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateCreateRecommendation),
  asyncHandler(createAdminRecommendationController),
);
router.delete(
  "/admin/recommendations/:id",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(deleteAdminRecommendationController),
);

export default router;
