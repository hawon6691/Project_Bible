import { Router } from "express";

import {
  approveFraudAlertController,
  getFraudAlertsController,
  getRealPriceController,
  rejectFraudAlertController,
} from "../fraud/fraud.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/products/:id/real-price", asyncHandler(getRealPriceController));
router.get("/fraud/alerts", requireAuth, requireRole("ADMIN"), asyncHandler(getFraudAlertsController));
router.patch("/fraud/alerts/:id/approve", requireAuth, requireRole("ADMIN"), asyncHandler(approveFraudAlertController));
router.patch("/fraud/alerts/:id/reject", requireAuth, requireRole("ADMIN"), asyncHandler(rejectFraudAlertController));

export default router;
