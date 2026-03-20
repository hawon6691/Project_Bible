import { Router } from "express";

import { getPriceTrendController } from "../prediction/prediction.controller.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/predictions/products/:productId/price-trend", asyncHandler(getPriceTrendController));

export default router;
