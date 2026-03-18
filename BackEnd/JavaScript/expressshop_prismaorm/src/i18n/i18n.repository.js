import { prisma } from "../prisma.js";

export function findTranslations(filters = {}) {
  return prisma.translation.findMany({
    where: {
      ...(filters.locale ? { locale: filters.locale } : {}),
      ...(filters.namespace ? { namespace: filters.namespace } : {}),
      ...(filters.key ? { key: filters.key } : {}),
    },
    orderBy: { id: "asc" },
  });
}

export function findTranslationById(id) {
  return prisma.translation.findUnique({
    where: { id: Number(id) },
  });
}

export function findTranslationByUnique(locale, namespace, key) {
  return prisma.translation.findUnique({
    where: {
      locale_namespace_key: {
        locale,
        namespace,
        key,
      },
    },
  });
}

export function createTranslation(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('translations', 'id'), COALESCE((SELECT MAX(id) FROM translations), 0) + 1, false)",
    );

    return tx.translation.create({ data });
  });
}

export function updateTranslation(id, data) {
  return prisma.translation.update({
    where: { id: Number(id) },
    data: {
      ...data,
      updatedAt: new Date(),
    },
  });
}

export function deleteTranslation(id) {
  return prisma.translation.delete({
    where: { id: Number(id) },
  });
}

export function findExchangeRates() {
  return prisma.exchangeRate.findMany({
    orderBy: [{ fetchedAt: "desc" }, { id: "desc" }],
  });
}

export function findExchangeRateByPair(baseCurrency, targetCurrency) {
  return prisma.exchangeRate.findUnique({
    where: {
      baseCurrency_targetCurrency: {
        baseCurrency,
        targetCurrency,
      },
    },
  });
}

export function createExchangeRate(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('exchange_rates', 'id'), COALESCE((SELECT MAX(id) FROM exchange_rates), 0) + 1, false)",
    );

    return tx.exchangeRate.create({ data });
  });
}

export function updateExchangeRate(id, data) {
  return prisma.exchangeRate.update({
    where: { id: Number(id) },
    data,
  });
}
