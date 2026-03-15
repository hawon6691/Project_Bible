import {
  getPriceHistory,
  getProduct,
  getProductPrices,
  getProducts,
  getProductSpecs,
  getSpecDefinitions,
} from "./product.service.js";
import {
  toPriceEntryDto,
  toPriceHistoryDto,
  toProductDetailDto,
  toProductSpecDto,
  toProductSummaryDto,
  toSpecDefinitionDto,
} from "./product.mapper.js";
import { success } from "../utils/response.js";

export async function getProductsController(req, res) {
  const { items, meta } = await getProducts(req.query);
  res.status(200).json(success(items.map(toProductSummaryDto), meta));
}

export async function getProductController(req, res) {
  const data = await getProduct(req.params.id);
  res.status(200).json(success(toProductDetailDto(data)));
}

export async function getSpecDefinitionsController(req, res) {
  const { items, meta } = await getSpecDefinitions(req.query);
  res.status(200).json(success(items.map(toSpecDefinitionDto), meta));
}

export async function getProductSpecsController(req, res) {
  const { items, meta } = await getProductSpecs(req.params.id);
  res.status(200).json(success(items.map(toProductSpecDto), meta));
}

export async function getProductPricesController(req, res) {
  const { items, meta } = await getProductPrices(req.params.id);
  res.status(200).json(success(items.map(toPriceEntryDto), meta));
}

export async function getPriceHistoryController(req, res) {
  const { items, meta } = await getPriceHistory(req.params.id);
  res.status(200).json(success(items.map(toPriceHistoryDto), meta));
}
