import { Router } from "express";

import {
  getPriceHistoryController,
  getProductController,
  getProductPricesController,
  getProductsController,
  getProductSpecsController,
  getSpecDefinitionsController,
} from "../products/product.controller.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/products", asyncHandler(getProductsController));
router.get("/products/:id", asyncHandler(getProductController));
router.get("/specs/definitions", asyncHandler(getSpecDefinitionsController));
router.get("/products/:id/specs", asyncHandler(getProductSpecsController));
router.get("/products/:id/prices", asyncHandler(getProductPricesController));
router.get("/products/:id/price-history", asyncHandler(getPriceHistoryController));

export default router;
