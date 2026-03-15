import { Router } from "express";

import {
  createReviewController,
  getReviewsController,
} from "../reviews/review.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateCreateReview } from "../reviews/review.validator.js";

const router = Router();

router.get("/products/:productId/reviews", asyncHandler(getReviewsController));
router.post(
  "/products/:productId/reviews",
  requireAuth,
  validate(validateCreateReview),
  asyncHandler(createReviewController),
);

export default router;
