import { Router } from "express";

import {
  deleteImageController,
  getImageVariantsController,
  uploadLegacyImageController,
  uploadImageController,
} from "../image/image.controller.js";
import { validateImageUploadBody } from "../image/image.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { uploadImageFile } from "../middleware/upload.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.post(
  "/images/upload",
  requireAuth,
  requireRole("USER", "ADMIN"),
  uploadImageFile,
  validate(validateImageUploadBody),
  asyncHandler(uploadImageController),
);
router.post(
  "/upload/image",
  requireAuth,
  requireRole("USER", "ADMIN"),
  uploadImageFile,
  asyncHandler(uploadLegacyImageController),
);
router.get("/images/:id/variants", asyncHandler(getImageVariantsController));
router.delete("/images/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteImageController));

export default router;
