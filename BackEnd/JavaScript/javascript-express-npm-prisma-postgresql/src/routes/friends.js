import { Router } from "express";
import {
  acceptFriendRequestController,
  blockUserController,
  getFriendFeedController,
  getFriendsController,
  getReceivedRequestsController,
  getSentRequestsController,
  rejectFriendRequestController,
  removeFriendController,
  requestFriendController,
  unblockUserController,
} from "../friends/friend.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.post("/friends/request/:userId", requireAuth, asyncHandler(requestFriendController));
router.patch("/friends/request/:friendshipId/accept", requireAuth, asyncHandler(acceptFriendRequestController));
router.patch("/friends/request/:friendshipId/reject", requireAuth, asyncHandler(rejectFriendRequestController));
router.get("/friends", requireAuth, asyncHandler(getFriendsController));
router.get("/friends/requests/received", requireAuth, asyncHandler(getReceivedRequestsController));
router.get("/friends/requests/sent", requireAuth, asyncHandler(getSentRequestsController));
router.get("/friends/feed", requireAuth, asyncHandler(getFriendFeedController));
router.post("/friends/block/:userId", requireAuth, asyncHandler(blockUserController));
router.delete("/friends/block/:userId", requireAuth, asyncHandler(unblockUserController));
router.delete("/friends/:userId", requireAuth, asyncHandler(removeFriendController));

export default router;
