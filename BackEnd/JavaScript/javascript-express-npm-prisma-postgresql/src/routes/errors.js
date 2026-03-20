import { Router } from "express";

import {
  getErrorCodeController,
  getErrorCodesController,
} from "../error-codes/error-code.controller.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/errors/codes", asyncHandler(getErrorCodesController));
router.get("/errors/codes/:key", asyncHandler(getErrorCodeController));

export default router;
