import { Router } from "express";
import {
  createNewsCategoryController,
  createNewsController,
  deleteNewsCategoryController,
  deleteNewsController,
  getNewsCategoriesController,
  getNewsController,
  getNewsDetailController,
  updateNewsController,
} from "../news/news.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateCreateNews, validateCreateNewsCategory } from "../news/news.validator.js";

const router = Router();

router.get("/news", asyncHandler(getNewsController));
router.get("/news/categories", asyncHandler(getNewsCategoriesController));
router.get("/news/:id", asyncHandler(getNewsDetailController));
router.post("/news", requireAuth, requireRole("ADMIN"), validate(validateCreateNews), asyncHandler(createNewsController));
router.patch("/news/:id", requireAuth, requireRole("ADMIN"), asyncHandler(updateNewsController));
router.delete("/news/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteNewsController));
router.post("/news/categories", requireAuth, requireRole("ADMIN"), validate(validateCreateNewsCategory), asyncHandler(createNewsCategoryController));
router.delete("/news/categories/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteNewsCategoryController));

export default router;
