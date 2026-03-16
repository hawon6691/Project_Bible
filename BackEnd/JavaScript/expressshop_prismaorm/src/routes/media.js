import { Router } from "express";
import {
  createPresignedUrlController,
  deleteMediaController,
  getMediaMetadataController,
  streamMediaController,
  uploadMediaController,
} from "../media/media.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.post("/media/upload", requireAuth, asyncHandler(uploadMediaController));
router.post("/media/presigned-url", requireAuth, asyncHandler(createPresignedUrlController));
router.get("/media/stream/:id", asyncHandler(streamMediaController));
router.delete("/media/:id", requireAuth, asyncHandler(deleteMediaController));
router.get("/media/:id/metadata", asyncHandler(getMediaMetadataController));

export default router;
