import { Router } from "express";

import {
  createAddressController,
  deleteAddressController,
  getAddressesController,
  updateAddressController,
} from "../addresses/address.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import { validateAddress } from "../addresses/address.validator.js";

const router = Router();

router.get("/addresses", requireAuth, asyncHandler(getAddressesController));
router.post("/addresses", requireAuth, validate(validateAddress), asyncHandler(createAddressController));
router.patch("/addresses/:id", requireAuth, validate(validateAddress), asyncHandler(updateAddressController));
router.delete("/addresses/:id", requireAuth, asyncHandler(deleteAddressController));

export default router;
