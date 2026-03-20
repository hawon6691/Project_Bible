import { Router } from "express";

import { getLowestEverController, getUnitPriceController } from "../analytics/analytics.controller.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/analytics/products/:id/lowest-ever", asyncHandler(getLowestEverController));
router.get("/analytics/products/:id/unit-price", asyncHandler(getUnitPriceController));

export default router;
