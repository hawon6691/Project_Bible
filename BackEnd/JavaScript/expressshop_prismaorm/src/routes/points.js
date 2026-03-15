import { Router } from "express";

import {
  getPointBalanceController,
  getPointTransactionsController,
  grantPointsController,
} from "../points/point.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateGrantPoints } from "../points/point.validator.js";

const router = Router();

router.get("/points/balance", requireAuth, asyncHandler(getPointBalanceController));
router.get("/points/transactions", requireAuth, asyncHandler(getPointTransactionsController));
router.post(
  "/admin/points/grant",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateGrantPoints),
  asyncHandler(grantPointsController),
);

export default router;
