import { Router } from "express";

import {
  closeRoomController,
  createRoomController,
  getRoomMessagesController,
  getRoomsController,
} from "../chat/chat.controller.js";
import { validateCreateRoom } from "../chat/chat.validator.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.post("/chat/rooms", requireAuth, validate(validateCreateRoom), asyncHandler(createRoomController));
router.get("/chat/rooms", requireAuth, asyncHandler(getRoomsController));
router.get("/chat/rooms/:id/messages", requireAuth, asyncHandler(getRoomMessagesController));
router.patch("/chat/rooms/:id/close", requireAuth, asyncHandler(closeRoomController));

export default router;
