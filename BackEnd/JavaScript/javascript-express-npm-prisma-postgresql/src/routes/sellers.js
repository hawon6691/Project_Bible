import { Router } from "express";

import {
  createSellerController,
  createSellerReviewController,
  deleteSellerController,
  getSellerController,
  getSellerReviewsController,
  getSellerTrustController,
  getSellersController,
  updateSellerReviewController,
  deleteSellerReviewController,
  updateSellerController,
} from "../sellers/seller.controller.js";
import {
  validateCreateSeller,
  validateCreateSellerReview,
  validateUpdateSeller,
  validateUpdateSellerReview,
} from "../sellers/seller.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/sellers", asyncHandler(getSellersController));
router.get("/sellers/:id", asyncHandler(getSellerController));
router.get("/sellers/:id/trust", asyncHandler(getSellerTrustController));
router.get("/sellers/:id/reviews", asyncHandler(getSellerReviewsController));
router.post(
  "/sellers/:id/reviews",
  requireAuth,
  validate(validateCreateSellerReview),
  asyncHandler(createSellerReviewController),
);
router.post("/sellers", requireAuth, requireRole("ADMIN"), validate(validateCreateSeller), asyncHandler(createSellerController));
router.patch("/sellers/:id", requireAuth, requireRole("ADMIN"), validate(validateUpdateSeller), asyncHandler(updateSellerController));
router.delete("/sellers/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteSellerController));
router.patch(
  "/seller-reviews/:id",
  requireAuth,
  validate(validateUpdateSellerReview),
  asyncHandler(updateSellerReviewController),
);
router.delete("/seller-reviews/:id", requireAuth, asyncHandler(deleteSellerReviewController));

export default router;
