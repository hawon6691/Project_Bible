import { Router } from "express";

import {
  addCompareItemController,
  getCompareDetailController,
  getCompareListController,
  removeCompareItemController,
} from "../compare/compare.controller.js";
import { validateAddCompareItem } from "../compare/compare.validator.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.post("/compare/add", validate(validateAddCompareItem), asyncHandler(addCompareItemController));
router.delete("/compare/:productId", asyncHandler(removeCompareItemController));
router.get("/compare", asyncHandler(getCompareListController));
router.get("/compare/detail", asyncHandler(getCompareDetailController));

export default router;
