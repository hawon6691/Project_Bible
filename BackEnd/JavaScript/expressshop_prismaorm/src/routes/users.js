import { Router } from "express";

import {
  getMeController,
  getProfileController,
  getUsersController,
  updateMeController,
} from "../users/user.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateUpdateMe } from "../users/user.validator.js";

const router = Router();

router.get("/users/me", requireAuth, asyncHandler(getMeController));
router.patch("/users/me", requireAuth, validate(validateUpdateMe), asyncHandler(updateMeController));
router.get("/users", requireAuth, requireRole("ADMIN"), asyncHandler(getUsersController));
router.get("/users/:id/profile", asyncHandler(getProfileController));

export default router;
