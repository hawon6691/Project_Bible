import { Router } from "express";

import {
  createBadgeController,
  deleteBadgeController,
  getBadgesController,
  getMyBadgesController,
  getUserBadgesController,
  grantBadgeController,
  revokeBadgeController,
  updateBadgeController,
} from "../badge/badge.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateCreateBadge,
  validateGrantBadge,
  validateUpdateBadge,
} from "../badge/badge.validator.js";

const router = Router();

router.get("/badges", asyncHandler(getBadgesController));
router.get("/badges/me", requireAuth, asyncHandler(getMyBadgesController));
router.get("/users/:id/badges", asyncHandler(getUserBadgesController));
router.post(
  "/admin/badges",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateCreateBadge),
  asyncHandler(createBadgeController),
);
router.patch(
  "/admin/badges/:id",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateBadge),
  asyncHandler(updateBadgeController),
);
router.delete(
  "/admin/badges/:id",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(deleteBadgeController),
);
router.post(
  "/admin/badges/:id/grant",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateGrantBadge),
  asyncHandler(grantBadgeController),
);
router.delete(
  "/admin/badges/:id/revoke/:userId",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(revokeBadgeController),
);

export default router;
