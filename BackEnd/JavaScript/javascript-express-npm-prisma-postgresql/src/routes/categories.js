import { Router } from "express";

import {
  createCategoryController,
  deleteCategoryController,
  getCategoriesController,
  getCategoryController,
  updateCategoryController,
} from "../categories/category.controller.js";
import { validateCreateCategory, validateUpdateCategory } from "../categories/category.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/categories", asyncHandler(getCategoriesController));
router.get("/categories/:id", asyncHandler(getCategoryController));
router.post(
  "/categories",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateCreateCategory),
  asyncHandler(createCategoryController),
);
router.patch(
  "/categories/:id",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateCategory),
  asyncHandler(updateCategoryController),
);
router.delete("/categories/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteCategoryController));

export default router;
