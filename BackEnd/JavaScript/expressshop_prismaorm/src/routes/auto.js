import { Router } from "express";

import { getCarModelsController, getLeaseOffersController } from "../auto/auto.controller.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/auto/models", asyncHandler(getCarModelsController));
router.get("/auto/models/:id/lease-offers", asyncHandler(getLeaseOffersController));

export default router;
