import { Router } from "express";

import {
  convertAmountController,
  deleteTranslationController,
  getExchangeRatesController,
  getTranslationsController,
  upsertExchangeRateController,
  upsertTranslationController,
} from "../i18n/i18n.controller.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";
import {
  validateConvertAmount,
  validateGetTranslations,
  validateUpsertExchangeRate,
  validateUpsertTranslation,
} from "../i18n/i18n.validator.js";

const router = Router();

router.get("/i18n/translations", validate(validateGetTranslations), asyncHandler(getTranslationsController));
router.post(
  "/admin/i18n/translations",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpsertTranslation),
  asyncHandler(upsertTranslationController),
);
router.delete(
  "/admin/i18n/translations/:id",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(deleteTranslationController),
);
router.get("/i18n/exchange-rates", asyncHandler(getExchangeRatesController));
router.post(
  "/admin/i18n/exchange-rates",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpsertExchangeRate),
  asyncHandler(upsertExchangeRateController),
);
router.get("/i18n/convert", validate(validateConvertAmount), asyncHandler(convertAmountController));

export default router;
