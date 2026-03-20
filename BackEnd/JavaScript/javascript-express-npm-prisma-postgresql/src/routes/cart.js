import { Router } from "express";

import {
  addCartItemController,
  clearCartController,
  deleteCartItemController,
  getCartController,
  updateCartItemController,
} from "../cart/cart.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateAddCartItem,
  validateUpdateCartItem,
} from "../cart/cart.validator.js";

const router = Router();

router.get("/cart", requireAuth, asyncHandler(getCartController));
router.post("/cart", requireAuth, validate(validateAddCartItem), asyncHandler(addCartItemController));
router.patch(
  "/cart/:itemId",
  requireAuth,
  validate(validateUpdateCartItem),
  asyncHandler(updateCartItemController),
);
router.delete("/cart/:itemId", requireAuth, asyncHandler(deleteCartItemController));
router.delete("/cart", requireAuth, asyncHandler(clearCartController));

export default router;
