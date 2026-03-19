import { Router } from "express";

import {
  findQueryProductDetailController,
  findQueryProductsController,
  rebuildQueryProductsController,
  syncQueryProductController,
} from "../query/query.controller.js";
import { validateQueryProducts } from "../query/query.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/query/products", validate(validateQueryProducts), asyncHandler(findQueryProductsController));
router.get("/query/products/:productId", asyncHandler(findQueryProductDetailController));

router.post(
  "/admin/query/products/:productId/sync",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(syncQueryProductController),
);
router.post(
  "/admin/query/products/rebuild",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(rebuildQueryProductsController),
);

export default router;
