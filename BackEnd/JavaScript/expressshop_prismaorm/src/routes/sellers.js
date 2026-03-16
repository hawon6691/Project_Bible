import { Router } from "express";

import {
  createSellerController,
  deleteSellerController,
  getSellerController,
  getSellersController,
  updateSellerController,
} from "../sellers/seller.controller.js";
import { validateCreateSeller, validateUpdateSeller } from "../sellers/seller.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/sellers", asyncHandler(getSellersController));
router.get("/sellers/:id", asyncHandler(getSellerController));
router.post("/sellers", requireAuth, requireRole("ADMIN"), validate(validateCreateSeller), asyncHandler(createSellerController));
router.patch("/sellers/:id", requireAuth, requireRole("ADMIN"), validate(validateUpdateSeller), asyncHandler(updateSellerController));
router.delete("/sellers/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteSellerController));

export default router;
