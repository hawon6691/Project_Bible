import { Router } from "express";

import {
  getAdminExtensionsController,
  getAdminReviewPolicyController,
  getAdminUploadLimitsController,
  updateAdminExtensionsController,
  updateAdminReviewPolicyController,
  updateAdminUploadLimitsController,
} from "../admin-settings/admin-settings.controller.js";
import {
  validateUpdateExtensions,
  validateUpdateReviewPolicy,
  validateUpdateUploadLimits,
} from "../admin-settings/admin-settings.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get(
  "/admin/settings/extensions",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getAdminExtensionsController),
);
router.post(
  "/admin/settings/extensions",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateExtensions),
  asyncHandler(updateAdminExtensionsController),
);
router.get(
  "/admin/settings/upload-limits",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getAdminUploadLimitsController),
);
router.patch(
  "/admin/settings/upload-limits",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateUploadLimits),
  asyncHandler(updateAdminUploadLimitsController),
);
router.get(
  "/admin/settings/review-policy",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getAdminReviewPolicyController),
);
router.patch(
  "/admin/settings/review-policy",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateReviewPolicy),
  asyncHandler(updateAdminReviewPolicyController),
);

export default router;
