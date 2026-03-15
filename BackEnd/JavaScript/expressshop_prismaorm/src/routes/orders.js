import { Router } from "express";

import {
  cancelOrderController,
  createOrderController,
  getAdminOrdersController,
  getOrderController,
  getOrdersController,
  updateOrderStatusController,
} from "../orders/order.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateCreateOrder,
  validateOrderStatus,
} from "../orders/order.validator.js";

const router = Router();

router.get("/orders", requireAuth, asyncHandler(getOrdersController));
router.get("/orders/:id", requireAuth, asyncHandler(getOrderController));
router.post("/orders", requireAuth, validate(validateCreateOrder), asyncHandler(createOrderController));
router.post("/orders/:id/cancel", requireAuth, asyncHandler(cancelOrderController));
router.get("/admin/orders", requireAuth, requireRole("ADMIN"), asyncHandler(getAdminOrdersController));
router.patch(
  "/admin/orders/:id/status",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateOrderStatus),
  asyncHandler(updateOrderStatusController),
);

export default router;
