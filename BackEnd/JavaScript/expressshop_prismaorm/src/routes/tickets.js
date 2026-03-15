import { Router } from "express";

import {
  createSupportReplyController,
  createSupportTicketController,
  getAdminSupportTicketsController,
  getSupportTicketController,
  getSupportTicketsController,
  updateSupportTicketStatusController,
} from "../tickets/ticket.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateCreateTicket,
  validateCreateTicketReply,
  validateTicketStatus,
} from "../tickets/ticket.validator.js";

const router = Router();

router.get("/support/tickets", requireAuth, asyncHandler(getSupportTicketsController));
router.post("/support/tickets", requireAuth, validate(validateCreateTicket), asyncHandler(createSupportTicketController));
router.get("/support/tickets/:id", requireAuth, asyncHandler(getSupportTicketController));
router.post(
  "/support/tickets/:id/reply",
  requireAuth,
  validate(validateCreateTicketReply),
  asyncHandler(createSupportReplyController),
);
router.get("/admin/support/tickets", requireAuth, requireRole("ADMIN"), asyncHandler(getAdminSupportTicketsController));
router.patch(
  "/admin/support/tickets/:id/status",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateTicketStatus),
  asyncHandler(updateSupportTicketStatusController),
);

export default router;
