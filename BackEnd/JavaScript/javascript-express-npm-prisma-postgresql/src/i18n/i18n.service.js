import { badRequest, notFound } from "../utils/http-error.js";
import {
  createExchangeRate,
  createTranslation,
  deleteTranslation as deleteTranslationRecord,
  findExchangeRateByPair,
  findExchangeRates,
  findTranslationById,
  findTranslationByUnique,
  findTranslations,
  updateExchangeRate,
  updateTranslation,
} from "./i18n.repository.js";

function normalizeLocale(locale) {
  return String(locale).trim().toLowerCase();
}

function normalizeCurrency(currency) {
  return String(currency).trim().toUpperCase();
}

function toTranslationDto(item) {
  return {
    id: item.id,
    locale: item.locale,
    namespace: item.namespace,
    key: item.key,
    value: item.value,
    updatedAt: item.updatedAt,
  };
}

function toExchangeRateDto(item) {
  return {
    id: item.id,
    baseCurrency: item.baseCurrency,
    targetCurrency: item.targetCurrency,
    rate: Number(item.rate),
    updatedAt: item.fetchedAt,
  };
}

export async function getTranslations(query) {
  const filters = {};
  if (query.locale) filters.locale = normalizeLocale(query.locale);
  if (query.namespace) filters.namespace = String(query.namespace).trim();
  if (query.key) filters.key = String(query.key).trim();

  const items = await findTranslations(filters);
  return items.map(toTranslationDto);
}

export async function upsertTranslation(payload) {
  const locale = normalizeLocale(payload.locale);
  const namespace = String(payload.namespace).trim();
  const key = String(payload.key).trim();
  const value = String(payload.value).trim();

  const existing = await findTranslationByUnique(locale, namespace, key);
  if (!existing) {
    const created = await createTranslation({
      locale,
      namespace,
      key,
      value,
    });
    return toTranslationDto(created);
  }

  const updated = await updateTranslation(existing.id, { value });
  return toTranslationDto(updated);
}

export async function deleteTranslation(id) {
  const existing = await findTranslationById(id);
  if (!existing) {
    throw notFound("Translation not found");
  }

  await deleteTranslationRecord(id);
  return { message: "Translation deleted" };
}

export async function getExchangeRates() {
  const items = await findExchangeRates();
  return items.map(toExchangeRateDto);
}

export async function upsertExchangeRate(payload) {
  const baseCurrency = normalizeCurrency(payload.baseCurrency);
  const targetCurrency = normalizeCurrency(payload.targetCurrency);

  if (baseCurrency === targetCurrency) {
    throw badRequest("baseCurrency and targetCurrency must be different");
  }

  const rate = Number(payload.rate);
  if (!Number.isFinite(rate) || rate <= 0) {
    throw badRequest("rate must be a positive number");
  }

  const source = payload.source ? String(payload.source).trim() : "manual";
  const fetchedAt = payload.fetchedAt ? new Date(payload.fetchedAt) : new Date();
  if (Number.isNaN(fetchedAt.getTime())) {
    throw badRequest("fetchedAt must be a valid date");
  }

  const existing = await findExchangeRateByPair(baseCurrency, targetCurrency);
  if (!existing) {
    const created = await createExchangeRate({
      baseCurrency,
      targetCurrency,
      rate,
      source,
      fetchedAt,
    });
    return toExchangeRateDto(created);
  }

  const updated = await updateExchangeRate(existing.id, {
    rate,
    source,
    fetchedAt,
  });
  return toExchangeRateDto(updated);
}

async function resolveDirectOrInverseRate(from, to) {
  const direct = await findExchangeRateByPair(from, to);
  if (direct) {
    return Number(direct.rate);
  }

  const inverse = await findExchangeRateByPair(to, from);
  if (inverse) {
    return Number((1 / Number(inverse.rate)).toFixed(8));
  }

  return null;
}

export async function convertAmount(query) {
  const amount = Number(query.amount);
  const from = normalizeCurrency(query.from);
  const to = normalizeCurrency(query.to);

  if (!Number.isFinite(amount) || amount < 0) {
    throw badRequest("amount must be a non-negative number");
  }

  let rate;
  if (from === to) {
    rate = 1;
  } else {
    rate = await resolveDirectOrInverseRate(from, to);
    if (rate == null) {
      const fromToKrw = await resolveDirectOrInverseRate(from, "KRW");
      const krwToTarget = await resolveDirectOrInverseRate("KRW", to);
      if (fromToKrw != null && krwToTarget != null) {
        rate = Number((fromToKrw * krwToTarget).toFixed(8));
      }
    }
  }

  if (rate == null) {
    throw notFound("Exchange rate not found");
  }

  return {
    originalAmount: amount,
    originalCurrency: from,
    convertedAmount: Number((amount * rate).toFixed(2)),
    targetCurrency: to,
    rate,
  };
}
