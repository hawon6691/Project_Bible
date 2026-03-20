import { Router } from "express";
import {
  createShortformCommentController,
  createShortformController,
  deleteShortformController,
  getShortformCommentsController,
  getShortformController,
  getShortformRankingController,
  getShortformsController,
  getTranscodeStatusController,
  getUserShortformsController,
  retryTranscodeController,
  toggleShortformLikeController,
} from "../shortforms/shortform.controller.js";
import { validate } from "../middleware/validate.js";
import { requireAuth } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateCreateShortform, validateCreateShortformComment } from "../shortforms/shortform.validator.js";

const router = Router();

router.post("/shortforms", requireAuth, validate(validateCreateShortform), asyncHandler(createShortformController));
router.get("/shortforms", asyncHandler(getShortformsController));
router.get("/shortforms/:id", asyncHandler(getShortformController));
router.post("/shortforms/:id/like", requireAuth, asyncHandler(toggleShortformLikeController));
router.post("/shortforms/:id/comments", requireAuth, validate(validateCreateShortformComment), asyncHandler(createShortformCommentController));
router.get("/shortforms/:id/comments", asyncHandler(getShortformCommentsController));
router.get("/shortforms/ranking/list", asyncHandler(getShortformRankingController));
router.get("/shortforms/:id/transcode-status", asyncHandler(getTranscodeStatusController));
router.post("/shortforms/:id/transcode/retry", requireAuth, asyncHandler(retryTranscodeController));
router.delete("/shortforms/:id", requireAuth, asyncHandler(deleteShortformController));
router.get("/shortforms/user/:userId", asyncHandler(getUserShortformsController));

export default router;
