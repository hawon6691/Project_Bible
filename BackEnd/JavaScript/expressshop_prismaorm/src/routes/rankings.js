import { Router } from "express";

import {
  getPopularProductsController,
  getPopularSearchesController,
  recalculateRankingsController,
} from "../rankings/ranking.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/rankings/products/popular", asyncHandler(getPopularProductsController));
router.get("/rankings/keywords/popular", asyncHandler(getPopularSearchesController));
router.get("/rankings/searches", asyncHandler(getPopularSearchesController));
router.post(
  "/rankings/admin/recalculate",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(recalculateRankingsController),
);

export default router;
