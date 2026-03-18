import { Router } from "express";

import {
  addPcBuildPartController,
  createCompatibilityRuleController,
  createPcBuildController,
  deleteCompatibilityRuleController,
  deletePcBuildController,
  getCompatibilityRulesController,
  getMyPcBuildsController,
  getPcBuildCompatibilityController,
  getPcBuildController,
  getPopularPcBuildsController,
  getSharedPcBuildController,
  removePcBuildPartController,
  sharePcBuildController,
  updateCompatibilityRuleController,
  updatePcBuildController,
} from "../pc-builder/pc-builder.controller.js";
import {
  validateAddPcBuildPart,
  validateCompatibilityRulePayload,
  validateCreatePcBuild,
  validatePcBuildListQuery,
  validateUpdatePcBuild,
} from "../pc-builder/pc-builder.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/pc-builds", requireAuth, validate(validatePcBuildListQuery), asyncHandler(getMyPcBuildsController));
router.post("/pc-builds", requireAuth, validate(validateCreatePcBuild), asyncHandler(createPcBuildController));
router.get("/pc-builds/popular", validate(validatePcBuildListQuery), asyncHandler(getPopularPcBuildsController));
router.get("/pc-builds/shared/:shareCode", asyncHandler(getSharedPcBuildController));
router.get("/pc-builds/:id/compatibility", asyncHandler(getPcBuildCompatibilityController));
router.get("/pc-builds/:id/share", requireAuth, asyncHandler(sharePcBuildController));
router.post(
  "/pc-builds/:id/parts",
  requireAuth,
  validate(validateAddPcBuildPart),
  asyncHandler(addPcBuildPartController),
);
router.delete("/pc-builds/:id/parts/:partId", requireAuth, asyncHandler(removePcBuildPartController));
router.get("/pc-builds/:id", asyncHandler(getPcBuildController));
router.patch("/pc-builds/:id", requireAuth, validate(validateUpdatePcBuild), asyncHandler(updatePcBuildController));
router.delete("/pc-builds/:id", requireAuth, asyncHandler(deletePcBuildController));

router.get("/admin/compatibility-rules", requireAuth, requireRole("ADMIN"), asyncHandler(getCompatibilityRulesController));
router.post(
  "/admin/compatibility-rules",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateCompatibilityRulePayload),
  asyncHandler(createCompatibilityRuleController),
);
router.patch(
  "/admin/compatibility-rules/:id",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateCompatibilityRulePayload),
  asyncHandler(updateCompatibilityRuleController),
);
router.delete(
  "/admin/compatibility-rules/:id",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(deleteCompatibilityRuleController),
);

export default router;
