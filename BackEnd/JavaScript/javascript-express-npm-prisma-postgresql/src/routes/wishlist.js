import { Router } from "express";

import {
  getWishlistController,
  removeWishlistController,
  toggleWishlistController,
} from "../wishlist/wishlist.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/wishlist", requireAuth, asyncHandler(getWishlistController));
router.post("/wishlist/:productId", requireAuth, asyncHandler(toggleWishlistController));
router.delete("/wishlist/:productId", requireAuth, asyncHandler(removeWishlistController));

export default router;
