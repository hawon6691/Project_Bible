import { Router } from "express";

import {
  createCommentController,
  createPostController,
  getBoardsController,
  getPostController,
  getPostsController,
} from "../boards/board.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateCreateComment,
  validateCreatePost,
} from "../boards/board.validator.js";

const router = Router();

router.get("/community/boards", asyncHandler(getBoardsController));
router.get("/community/posts", asyncHandler(getPostsController));
router.post("/community/posts", requireAuth, validate(validateCreatePost), asyncHandler(createPostController));
router.get("/community/posts/:id", asyncHandler(getPostController));
router.post(
  "/community/posts/:id/comments",
  requireAuth,
  validate(validateCreateComment),
  asyncHandler(createCommentController),
);

export default router;
