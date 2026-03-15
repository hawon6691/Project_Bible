import { Router } from "express";

import {
  loginController,
  logoutController,
  meController,
  refreshController,
  signupController,
} from "../auth/auth.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateLogin,
  validateRefresh,
  validateSignup,
} from "../auth/auth.validator.js";

const router = Router();

router.post("/auth/signup", validate(validateSignup), asyncHandler(signupController));
router.post("/auth/login", validate(validateLogin), asyncHandler(loginController));
router.post("/auth/logout", requireAuth, asyncHandler(logoutController));
router.post("/auth/refresh", validate(validateRefresh), asyncHandler(refreshController));
router.get("/auth/me", requireAuth, asyncHandler(meController));

export default router;
