import {
  convertAmount,
  deleteTranslation,
  getExchangeRates,
  getTranslations,
  upsertExchangeRate,
  upsertTranslation,
} from "./i18n.service.js";
import { success } from "../utils/response.js";

export async function getTranslationsController(req, res) {
  const data = await getTranslations(req.query);
  res.status(200).json(success(data));
}

export async function upsertTranslationController(req, res) {
  const data = await upsertTranslation(req.body);
  res.status(201).json(success(data));
}

export async function deleteTranslationController(req, res) {
  const data = await deleteTranslation(req.params.id);
  res.status(200).json(success(data));
}

export async function getExchangeRatesController(_req, res) {
  const data = await getExchangeRates();
  res.status(200).json(success(data));
}

export async function upsertExchangeRateController(req, res) {
  const data = await upsertExchangeRate(req.body);
  res.status(201).json(success(data));
}

export async function convertAmountController(req, res) {
  const data = await convertAmount(req.query);
  res.status(200).json(success(data));
}
