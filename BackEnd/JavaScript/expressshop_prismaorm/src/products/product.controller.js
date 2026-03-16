import {
  compareScoredSpecs,
  compareSpecs,
  createAdminProduct,
  createAdminProductOption,
  createAdminSpecDefinition,
  createPriceAlert,
  createProductPrice,
  deleteAdminProduct,
  deleteAdminProductOption,
  deleteAdminSpecDefinition,
  deletePriceAlert,
  deleteProductPrice,
  getPriceAlerts,
  getPriceHistory,
  getProduct,
  getProductPrices,
  getProducts,
  getProductSpecs,
  getSpecDefinitions,
  setProductSpecs,
  updateAdminProduct,
  updateAdminProductOption,
  updateAdminSpecDefinition,
  updateProductPrice,
  updateSpecScores,
} from "./product.service.js";
import {
  toPriceAlertDto,
  toPriceEntryDto,
  toPriceHistoryDto,
  toProductDetailDto,
  toProductOptionDto,
  toProductSpecDto,
  toProductSummaryDto,
  toSpecDefinitionDto,
  toSpecScoreDto,
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

export async function createProductController(req, res) {
  const data = await createAdminProduct(req.body);
  res.status(201).json(success(toProductDetailDto(data)));
}

export async function updateProductController(req, res) {
  const data = await updateAdminProduct(req.params.id, req.body);
  res.status(200).json(success(toProductDetailDto(data)));
}

export async function deleteProductController(req, res) {
  const data = await deleteAdminProduct(req.params.id);
  res.status(200).json(success(data));
}

export async function createProductOptionController(req, res) {
  const data = await createAdminProductOption(req.params.id, req.body);
  res.status(201).json(success(toProductOptionDto(data)));
}

export async function updateProductOptionController(req, res) {
  const data = await updateAdminProductOption(req.params.id, req.params.optionId, req.body);
  res.status(200).json(success(toProductOptionDto(data)));
}

export async function deleteProductOptionController(req, res) {
  const data = await deleteAdminProductOption(req.params.id, req.params.optionId);
  res.status(200).json(success(data));
}

export async function getSpecDefinitionsController(req, res) {
  const { items, meta } = await getSpecDefinitions(req.query);
  res.status(200).json(success(items.map(toSpecDefinitionDto), meta));
}

export async function createSpecDefinitionController(req, res) {
  const data = await createAdminSpecDefinition(req.body);
  res.status(201).json(success(toSpecDefinitionDto(data)));
}

export async function updateSpecDefinitionController(req, res) {
  const data = await updateAdminSpecDefinition(req.params.id, req.body);
  res.status(200).json(success(toSpecDefinitionDto(data)));
}

export async function deleteSpecDefinitionController(req, res) {
  const data = await deleteAdminSpecDefinition(req.params.id);
  res.status(200).json(success(data));
}

export async function getProductSpecsController(req, res) {
  const { items, meta } = await getProductSpecs(req.params.id);
  res.status(200).json(success(items.map(toProductSpecDto), meta));
}

export async function setProductSpecsController(req, res) {
  const { items, meta } = await setProductSpecs(req.params.id, req.body);
  res.status(200).json(success(items.map(toProductSpecDto), meta));
}

export async function compareSpecsController(req, res) {
  const data = await compareSpecs(req.body);
  res.status(200).json(success(data));
}

export async function compareScoredSpecsController(req, res) {
  const data = await compareScoredSpecs(req.body);
  res.status(200).json(success(data));
}

export async function updateSpecScoresController(req, res) {
  const data = await updateSpecScores(req.params.specDefId, req.body);
  res.status(200).json(success(data.map(toSpecScoreDto)));
}

export async function getProductPricesController(req, res) {
  const { items, meta } = await getProductPrices(req.params.id);
  res.status(200).json(success(items.map(toPriceEntryDto), meta));
}

export async function createProductPriceController(req, res) {
  const data = await createProductPrice(req.params.id, req.body);
  res.status(201).json(success(toPriceEntryDto(data)));
}

export async function updateProductPriceController(req, res) {
  const data = await updateProductPrice(req.params.id, req.body);
  res.status(200).json(success(toPriceEntryDto(data)));
}

export async function deleteProductPriceController(req, res) {
  const data = await deleteProductPrice(req.params.id);
  res.status(200).json(success(data));
}

export async function getPriceHistoryController(req, res) {
  const { items, meta } = await getPriceHistory(req.params.id);
  res.status(200).json(success(items.map(toPriceHistoryDto), meta));
}

export async function getPriceAlertsController(req, res) {
  const { items, meta } = await getPriceAlerts(req.user.id);
  res.status(200).json(success(items.map(toPriceAlertDto), meta));
}

export async function createPriceAlertController(req, res) {
  const data = await createPriceAlert(req.user.id, req.body);
  res.status(201).json(success(toPriceAlertDto(data)));
}

export async function deletePriceAlertController(req, res) {
  const data = await deletePriceAlert(req.user.id, req.params.id);
  res.status(200).json(success(data));
}
