function isNonEmpty(value) {
  return value !== undefined && value !== null && String(value).trim() !== "";
}

export function validateGetTranslations(req) {
  const { locale, namespace, key } = req.query ?? {};
  if (locale !== undefined && !isNonEmpty(locale)) return "locale must not be empty";
  if (namespace !== undefined && !isNonEmpty(namespace)) return "namespace must not be empty";
  if (key !== undefined && !isNonEmpty(key)) return "key must not be empty";
  return null;
}

export function validateUpsertTranslation(req) {
  const { locale, namespace, key, value } = req.body ?? {};
  return isNonEmpty(locale) && isNonEmpty(namespace) && isNonEmpty(key) && isNonEmpty(value)
    ? null
    : "locale, namespace, key, value are required";
}

export function validateUpsertExchangeRate(req) {
  const { baseCurrency, targetCurrency, rate, source, fetchedAt } = req.body ?? {};
  if (!isNonEmpty(baseCurrency) || !isNonEmpty(targetCurrency) || rate === undefined || rate === null) {
    return "baseCurrency, targetCurrency, rate are required";
  }

  const parsedRate = Number(rate);
  if (!Number.isFinite(parsedRate) || parsedRate <= 0) {
    return "rate must be a positive number";
  }
  if (source !== undefined && !isNonEmpty(source)) return "source must not be empty";
  if (fetchedAt !== undefined && Number.isNaN(new Date(fetchedAt).getTime())) return "fetchedAt must be a valid date";
  return null;
}

export function validateConvertAmount(req) {
  const { amount, from, to } = req.query ?? {};
  if (amount === undefined || amount === null || !isNonEmpty(from) || !isNonEmpty(to)) {
    return "amount, from, to are required";
  }

  const parsedAmount = Number(amount);
  return Number.isFinite(parsedAmount) && parsedAmount >= 0
    ? null
    : "amount must be a non-negative number";
}
