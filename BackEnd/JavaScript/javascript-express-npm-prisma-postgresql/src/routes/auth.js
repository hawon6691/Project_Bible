import { Router } from "express";

import {
  confirmPasswordResetController,
  loginController,
  logoutController,
  meController,
  requestPasswordResetController,
  refreshController,
  resendVerificationController,
  signupController,
  verifyEmailController,
  verifyPasswordResetController,
} from "../auth/auth.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateLogin,
  validatePasswordResetConfirm,
  validatePasswordResetRequest,
  validatePasswordResetVerify,
  validateRefresh,
  validateResendVerification,
  validateSignup,
  validateVerifyEmail,
} from "../auth/auth.validator.js";

const router = Router();

router.post("/auth/signup", validate(validateSignup), asyncHandler(signupController));
router.post("/auth/verify-email", validate(validateVerifyEmail), asyncHandler(verifyEmailController));
router.post("/auth/resend-verification", validate(validateResendVerification), asyncHandler(resendVerificationController));
router.post("/auth/login", validate(validateLogin), asyncHandler(loginController));
router.post(
  "/auth/password-reset/request",
  validate(validatePasswordResetRequest),
  asyncHandler(requestPasswordResetController),
);
router.post(
  "/auth/password-reset/verify",
  validate(validatePasswordResetVerify),
  asyncHandler(verifyPasswordResetController),
);
router.post(
  "/auth/password-reset/confirm",
  validate(validatePasswordResetConfirm),
  asyncHandler(confirmPasswordResetController),
);
router.post("/auth/logout", requireAuth, asyncHandler(logoutController));
router.post("/auth/refresh", validate(validateRefresh), asyncHandler(refreshController));
router.get("/auth/me", requireAuth, asyncHandler(meController));

export default router;
