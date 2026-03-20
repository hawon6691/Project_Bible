import { Router } from "express";

import {
  createInquiryController,
  getInquiriesController,
} from "../inquiries/inquiry.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateCreateInquiry } from "../inquiries/inquiry.validator.js";

const router = Router();

router.get("/products/:productId/inquiries", asyncHandler(getInquiriesController));
router.post(
  "/products/:productId/inquiries",
  requireAuth,
  validate(validateCreateInquiry),
  asyncHandler(createInquiryController),
);

export default router;
