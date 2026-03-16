import { Router } from "express";

import {
  createDealController,
  deleteDealController,
  getDealController,
  getDealsController,
  updateDealController,
} from "../deals/deal.controller.js";
import { validateCreateDeal } from "../deals/deal.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/deals", asyncHandler(getDealsController));
router.get("/deals/:id", asyncHandler(getDealController));
router.post("/deals", requireAuth, requireRole("ADMIN"), validate(validateCreateDeal), asyncHandler(createDealController));
router.patch("/deals/:id", requireAuth, requireRole("ADMIN"), asyncHandler(updateDealController));
router.delete("/deals/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteDealController));

export default router;
