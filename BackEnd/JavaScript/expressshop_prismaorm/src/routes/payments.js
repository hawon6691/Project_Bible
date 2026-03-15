import { Router } from "express";

import {
  createPaymentController,
  getPaymentController,
  refundPaymentController,
} from "../payments/payment.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateCreatePayment } from "../payments/payment.validator.js";

const router = Router();

router.post("/payments", requireAuth, validate(validateCreatePayment), asyncHandler(createPaymentController));
router.get("/payments/:id", requireAuth, asyncHandler(getPaymentController));
router.post("/payments/:id/refund", requireAuth, asyncHandler(refundPaymentController));

export default router;
