import { Router } from "express";

import {
  estimatePcBuildUsedPriceController,
  getCategoryUsedPricesController,
  getProductUsedPriceController,
} from "../used-market/used-market.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/used-market/products/:id/price", asyncHandler(getProductUsedPriceController));
router.get("/used-market/categories/:id/prices", asyncHandler(getCategoryUsedPricesController));
router.post(
  "/used-market/pc-builds/:buildId/estimate",
  requireAuth,
  requireRole("USER", "ADMIN"),
  asyncHandler(estimatePcBuildUsedPriceController),
);

export default router;
