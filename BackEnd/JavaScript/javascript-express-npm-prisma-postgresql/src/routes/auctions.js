import { Router } from "express";

import {
  cancelAuctionController,
  createAuctionBidController,
  createAuctionController,
  deleteAuctionBidController,
  getAuctionController,
  getAuctionsController,
  selectAuctionBidController,
  updateAuctionBidController,
} from "../auctions/auction.controller.js";
import {
  validateCreateAuction,
  validateCreateAuctionBid,
  validateUpdateAuctionBid,
} from "../auctions/auction.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/auctions", asyncHandler(getAuctionsController));
router.get("/auctions/:id", asyncHandler(getAuctionController));
router.post(
  "/auctions",
  requireAuth,
  requireRole("USER", "ADMIN"),
  validate(validateCreateAuction),
  asyncHandler(createAuctionController),
);
router.post(
  "/auctions/:id/bids",
  requireAuth,
  requireRole("SELLER"),
  validate(validateCreateAuctionBid),
  asyncHandler(createAuctionBidController),
);
router.patch(
  "/auctions/:id/bids/:bidId/select",
  requireAuth,
  requireRole("USER", "ADMIN"),
  asyncHandler(selectAuctionBidController),
);
router.delete(
  "/auctions/:id",
  requireAuth,
  requireRole("USER", "ADMIN"),
  asyncHandler(cancelAuctionController),
);
router.patch(
  "/auctions/:id/bids/:bidId",
  requireAuth,
  requireRole("SELLER"),
  validate(validateUpdateAuctionBid),
  asyncHandler(updateAuctionBidController),
);
router.delete(
  "/auctions/:id/bids/:bidId",
  requireAuth,
  requireRole("SELLER"),
  asyncHandler(deleteAuctionBidController),
);

export default router;
