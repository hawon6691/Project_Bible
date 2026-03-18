import { Router } from "express";

import {
  deleteMeController,
  deleteMyProfileImageController,
  getMeController,
  getProfileController,
  getUsersController,
  uploadMyProfileImageController,
  updateMyProfileController,
  updateMeController,
  updateUserStatusController,
} from "../users/user.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { uploadImageFile } from "../middleware/upload.js";
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
router.post(
  "/users/me/profile-image",
  requireAuth,
  requireRole("USER", "SELLER", "ADMIN"),
  uploadImageFile,
  asyncHandler(uploadMyProfileImageController),
);
router.delete("/users/me/profile-image", requireAuth, asyncHandler(deleteMyProfileImageController));

export default router;
