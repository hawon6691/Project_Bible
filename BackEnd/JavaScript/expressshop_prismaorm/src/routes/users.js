import { Router } from "express";

import {
  deleteMeController,
  getMeController,
  getProfileController,
  getUsersController,
  updateMyProfileController,
  updateMeController,
  updateUserStatusController,
} from "../users/user.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateUpdateMe,
  validateUpdateMyProfile,
  validateUpdateUserStatus,
} from "../users/user.validator.js";

const router = Router();

router.get("/users/me", requireAuth, asyncHandler(getMeController));
router.patch("/users/me", requireAuth, validate(validateUpdateMe), asyncHandler(updateMeController));
router.delete("/users/me", requireAuth, asyncHandler(deleteMeController));
router.get("/users", requireAuth, requireRole("ADMIN"), asyncHandler(getUsersController));
router.patch(
  "/users/:id/status",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateUserStatus),
  asyncHandler(updateUserStatusController),
);
router.get("/users/:id/profile", asyncHandler(getProfileController));
router.patch(
  "/users/me/profile",
  requireAuth,
  validate(validateUpdateMyProfile),
  asyncHandler(updateMyProfileController),
);

export default router;
