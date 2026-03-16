import { Router } from "express";

import {
  compareScoredSpecsController,
  compareSpecsController,
  createPriceAlertController,
  createProductController,
  createProductOptionController,
  createProductPriceController,
  createSpecDefinitionController,
  deletePriceAlertController,
  deleteProductController,
  deleteProductOptionController,
  deleteProductPriceController,
  deleteSpecDefinitionController,
  getPriceHistoryController,
  getPriceAlertsController,
  getProductController,
  getProductPricesController,
  getProductsController,
  getProductSpecsController,
  getSpecDefinitionsController,
  setProductSpecsController,
  updateProductController,
  updateProductOptionController,
  updateProductPriceController,
  updateSpecDefinitionController,
  updateSpecScoresController,
} from "../products/product.controller.js";
import {
  validateCompareSpecs,
  validateCreatePriceAlert,
  validateCreatePriceEntry,
  validateCreateProduct,
  validateCreateProductOption,
  validateCreateSpecDefinition,
  validateSetProductSpecs,
  validateUpdatePriceEntry,
  validateUpdateProduct,
  validateUpdateProductOption,
  validateUpdateSpecDefinition,
  validateUpdateSpecScores,
} from "../products/product.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/products", asyncHandler(getProductsController));
router.get("/products/:id", asyncHandler(getProductController));
router.post("/products", requireAuth, requireRole("ADMIN"), validate(validateCreateProduct), asyncHandler(createProductController));
router.patch("/products/:id", requireAuth, requireRole("ADMIN"), validate(validateUpdateProduct), asyncHandler(updateProductController));
router.delete("/products/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteProductController));
router.post(
  "/products/:id/options",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateCreateProductOption),
  asyncHandler(createProductOptionController),
);
router.patch(
  "/products/:id/options/:optionId",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateProductOption),
  asyncHandler(updateProductOptionController),
);
router.delete(
  "/products/:id/options/:optionId",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(deleteProductOptionController),
);
router.get("/specs/definitions", asyncHandler(getSpecDefinitionsController));
router.post(
  "/specs/definitions",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateCreateSpecDefinition),
  asyncHandler(createSpecDefinitionController),
);
router.patch(
  "/specs/definitions/:id",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateSpecDefinition),
  asyncHandler(updateSpecDefinitionController),
);
router.delete("/specs/definitions/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteSpecDefinitionController));
router.get("/products/:id/specs", asyncHandler(getProductSpecsController));
router.put(
  "/products/:id/specs",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateSetProductSpecs),
  asyncHandler(setProductSpecsController),
);
router.post("/specs/compare", validate(validateCompareSpecs), asyncHandler(compareSpecsController));
router.post("/specs/compare/scored", validate(validateCompareSpecs), asyncHandler(compareScoredSpecsController));
router.put(
  "/specs/scores/:specDefId",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateSpecScores),
  asyncHandler(updateSpecScoresController),
);
router.get("/products/:id/prices", asyncHandler(getProductPricesController));
router.post(
  "/products/:id/prices",
  requireAuth,
  requireRole("SELLER", "ADMIN"),
  validate(validateCreatePriceEntry),
  asyncHandler(createProductPriceController),
);
router.patch(
  "/prices/:id",
  requireAuth,
  requireRole("SELLER", "ADMIN"),
  validate(validateUpdatePriceEntry),
  asyncHandler(updateProductPriceController),
);
router.delete("/prices/:id", requireAuth, requireRole("ADMIN"), asyncHandler(deleteProductPriceController));
router.get("/products/:id/price-history", asyncHandler(getPriceHistoryController));
router.get("/price-alerts", requireAuth, asyncHandler(getPriceAlertsController));
router.post("/price-alerts", requireAuth, validate(validateCreatePriceAlert), asyncHandler(createPriceAlertController));
router.delete("/price-alerts/:id", requireAuth, asyncHandler(deletePriceAlertController));

export default router;
