import { Router } from "express";

import {
  createFaqController,
  getFaqsController,
} from "../faqs/faq.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateCreateFaq } from "../faqs/faq.validator.js";

const router = Router();

router.get("/faqs", asyncHandler(getFaqsController));
router.post("/faqs", requireAuth, requireRole("ADMIN"), validate(validateCreateFaq), asyncHandler(createFaqController));

export default router;
