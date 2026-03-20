import {
  clearRecentSearchKeywords,
  countProductQueryViews,
  countSearchIndexOutbox,
  countSearchIndexOutboxByStatus,
  createRecentSearchKeyword,
  createSearchIndexOutbox,
  createSearchLog,
  deleteOverflowRecentSearchKeywords,
  deleteRecentSearchKeyword,
  findActiveSearchSynonyms,
  findAutocompleteCategories,
  findAutocompleteProducts,
  findFailedSearchIndexOutbox,
  findPopularKeywordsMatching,
  findPopularSearchLogs,
  findProductForSearchIndex,
  findProductsForSearchIndex,
  findRecentSearchKeywordById,
  findRecentSearchKeywordByUser,
  findRecentSearchKeywords,
  findSearchProducts,
  findSearchWeightSettings,
  requeueFailedSearchIndexOutbox,
  updateRecentSearchKeyword,
  upsertProductQueryView,
  upsertSearchWeightSetting,
} from "./search.repository.js";
import { highlightText } from "./search.mapper.js";
import { findUserById, updateUser } from "../users/user.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

const DEFAULT_LIMIT = 20;
const MAX_LIMIT = 100;
const MAX_AUTOCOMPLETE_LIMIT = 10;
const DEFAULT_RECENT_LIMIT = 10;
const ALLOWED_SORTS = new Set(["relevance", "price_asc", "price_desc", "newest", "popularity"]);
const DEFAULT_WEIGHTS = {
  nameWeight: 10,
  keywordWeight: 4,
  clickWeight: 2,
};
const PRICE_RANGES = [
  { label: "50만원 이하", min: 0, max: 500000 },
  { label: "50~100만원", min: 500000, max: 1000000 },
  { label: "100~200만원", min: 1000000, max: 2000000 },
  { label: "200만원 이상", min: 2000000, max: null },
];

function toFiniteNumber(value, fallback = 0) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function normalizeLimit(value, fallback, max) {
  const parsed = Number(value ?? fallback);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.min(Math.trunc(parsed), max);
}

function normalizePage(value) {
  const parsed = Number(value ?? 1);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return 1;
  }
  return Math.trunc(parsed);
}

function normalizeSort(value) {
  const sort = String(value ?? "relevance");
  return ALLOWED_SORTS.has(sort) ? sort : "relevance";
}

function toEffectivePrice(item) {
  return item.lowestPrice ?? item.price ?? null;
}

function normalizeSpecValue(item) {
  if (item.value) {
    return String(item.value);
  }
  if (item.numericValue !== null && item.numericValue !== undefined) {
    const suffix = item.specDefinition?.unit ? String(item.specDefinition.unit) : "";
    return `${item.numericValue}${suffix}`;
  }
  return null;
}

function parseSpecFilters(rawSpecs) {
  if (!rawSpecs) {
    return {};
  }

  let parsed = rawSpecs;
  if (typeof rawSpecs === "string") {
    try {
      parsed = JSON.parse(rawSpecs);
    } catch {
      throw badRequest("specs must be valid JSON");
    }
  }

  if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) {
    throw badRequest("specs must be an object");
  }

  return Object.fromEntries(
    Object.entries(parsed)
      .map(([key, value]) => [
        String(key),
        Array.isArray(value)
          ? value.map((item) => String(item).trim()).filter(Boolean)
          : [String(value).trim()].filter(Boolean),
      ])
      .filter(([, values]) => values.length > 0),
  );
}

function matchesSpecFilters(item, specFilters) {
  const entries = Object.entries(specFilters);
  if (entries.length === 0) {
    return true;
  }

  return entries.every(([specName, values]) =>
    (item.specs ?? []).some((spec) => {
      if (String(spec.specDefinition?.name ?? "").toLowerCase() !== specName.toLowerCase()) {
        return false;
      }

      const displayValue = normalizeSpecValue(spec);
      return displayValue !== null && values.some((value) => displayValue.toLowerCase() === value.toLowerCase());
    }),
  );
}

function computeSearchScore(item, keyword, weights) {
  const q = String(keyword ?? "").trim().toLowerCase();
  const name = String(item.name ?? "").toLowerCase();
  const description = String(item.description ?? "").toLowerCase();
  const categoryName = String(item.category?.name ?? "").toLowerCase();
  const popularity = toFiniteNumber(item.popularityScore);
  const reviewCount = toFiniteNumber(item.reviewCount);
  const viewCount = toFiniteNumber(item.viewCount);

  let score = 0;
  if (name === q) {
    score += weights.nameWeight * 2;
  } else if (name.includes(q)) {
    score += weights.nameWeight;
  }

  if (description.includes(q)) {
    score += weights.keywordWeight;
  }

  if (categoryName.includes(q)) {
    score += Math.max(1, weights.keywordWeight / 2);
  }

  score += Math.min(popularity / 10, weights.clickWeight * 5);
  score += Math.min(reviewCount / 10, weights.clickWeight * 2);
  score += Math.min(viewCount / 100, weights.clickWeight * 2);

  return Number(score.toFixed(2));
}

function compareSearchItems(left, right, sort) {
  const leftPrice = toEffectivePrice(left);
  const rightPrice = toEffectivePrice(right);

  switch (sort) {
    case "price_asc":
      return (leftPrice ?? Number.MAX_SAFE_INTEGER) - (rightPrice ?? Number.MAX_SAFE_INTEGER) || right.id - left.id;
    case "price_desc":
      return (rightPrice ?? 0) - (leftPrice ?? 0) || right.id - left.id;
    case "newest":
      return right.id - left.id;
    case "popularity":
      return toFiniteNumber(right.popularityScore) - toFiniteNumber(left.popularityScore) || right.id - left.id;
    case "relevance":
    default:
      return right.score - left.score || toFiniteNumber(right.popularityScore) - toFiniteNumber(left.popularityScore) || right.id - left.id;
  }
}

function buildFacetSummary(items) {
  const categoryCounts = new Map();
  const specCounts = new Map();

  for (const item of items) {
    const categoryKey = `${item.category?.id ?? 0}:${item.category?.name ?? "기타"}`;
    categoryCounts.set(categoryKey, (categoryCounts.get(categoryKey) ?? 0) + 1);

    for (const spec of item.specs ?? []) {
      const specName = spec.specDefinition?.name;
      const specValue = normalizeSpecValue(spec);
      if (!specName || !specValue) {
        continue;
      }

      if (!specCounts.has(specName)) {
        specCounts.set(specName, new Map());
      }
      const bucket = specCounts.get(specName);
      bucket.set(specValue, (bucket.get(specValue) ?? 0) + 1);
    }
  }

  const categories = [...categoryCounts.entries()]
    .map(([key, count]) => {
      const [id, name] = key.split(":");
      return { id: Number(id), name, count };
    })
    .sort((a, b) => b.count - a.count || a.id - b.id);

  const priceRanges = PRICE_RANGES.map((range) => {
    const count = items.filter((item) => {
      const price = toEffectivePrice(item);
      if (price === null) {
        return false;
      }
      if (range.max === null) {
        return price >= range.min;
      }
      return price >= range.min && price < range.max;
    }).length;

    return {
      label: range.label,
      min: range.min,
      max: range.max,
      count,
    };
  });

  const specs = Object.fromEntries(
    [...specCounts.entries()].map(([name, counts]) => [
      name,
      [...counts.entries()]
        .map(([value, count]) => ({ value, count }))
        .sort((a, b) => b.count - a.count || a.value.localeCompare(b.value))
        .slice(0, 10),
    ]),
  );

  return {
    categories,
    priceRanges,
    specs,
  };
}

function collectSuggestions(keyword, products, synonymRows, popularRows) {
  const needle = String(keyword ?? "").trim().toLowerCase();
  const values = [];

  for (const row of synonymRows) {
    values.push(row.word);
    if (Array.isArray(row.synonyms)) {
      values.push(...row.synonyms);
    }
  }

  values.push(...products.map((item) => item.name));
  values.push(...popularRows.map((item) => item.keyword));

  return [...new Set(values.map((item) => String(item).trim()).filter(Boolean))]
    .filter((item) => item.toLowerCase().includes(needle))
    .slice(0, 5);
}

async function getWeightConfig() {
  const rows = await findSearchWeightSettings();
  if (rows.length === 0) {
    await Promise.all(
      Object.entries(DEFAULT_WEIGHTS).map(([field, weight]) => upsertSearchWeightSetting(field, weight)),
    );
    return { ...DEFAULT_WEIGHTS };
  }

  return {
    nameWeight: rows.find((item) => item.field === "nameWeight")?.weight ?? DEFAULT_WEIGHTS.nameWeight,
    keywordWeight: rows.find((item) => item.field === "keywordWeight")?.weight ?? DEFAULT_WEIGHTS.keywordWeight,
    clickWeight: rows.find((item) => item.field === "clickWeight")?.weight ?? DEFAULT_WEIGHTS.clickWeight,
  };
}

async function ensureUser(userId) {
  const user = await findUserById(userId);
  if (!user || user.deletedAt) {
    throw notFound("User not found");
  }
  return user;
}

function toQueryViewPayload(product) {
  return {
    productId: Number(product.id),
    categoryId: Number(product.categoryId),
    name: product.name,
    thumbnailUrl: product.thumbnailUrl ?? null,
    status: String(product.status),
    basePrice: Number(product.price),
    lowestPrice: product.lowestPrice === null || product.lowestPrice === undefined ? null : Number(product.lowestPrice),
    sellerCount: Number(product.sellerCount ?? 0),
    averageRating: Number(product.averageRating ?? 0),
    reviewCount: Number(product.reviewCount ?? 0),
    viewCount: Number(product.viewCount ?? 0),
    popularityScore: Number(product.popularityScore ?? 0),
    syncedAt: new Date(),
  };
}

async function writeCompletedOutbox(aggregateId, payload) {
  return createSearchIndexOutbox({
    eventType: "PRODUCT_REINDEX",
    status: "COMPLETED",
    aggregateId: Number(aggregateId),
    payload,
    attemptCount: 1,
    processedAt: new Date(),
  });
}

export async function search(query) {
  const keyword = String(query.q ?? "").trim();
  if (!keyword) {
    throw badRequest("q is required");
  }

  const page = normalizePage(query.page);
  const limit = normalizeLimit(query.limit, DEFAULT_LIMIT, MAX_LIMIT);
  const categoryId = query.categoryId ? Number(query.categoryId) : null;
  const minPrice = query.minPrice !== undefined ? Number(query.minPrice) : null;
  const maxPrice = query.maxPrice !== undefined ? Number(query.maxPrice) : null;
  const sort = normalizeSort(query.sort);
  const specFilters = parseSpecFilters(query.specs);
  const startedAt = Date.now();

  const [weights, candidates, synonymRows, popularRows] = await Promise.all([
    getWeightConfig(),
    findSearchProducts(keyword, categoryId),
    findActiveSearchSynonyms(keyword, 5),
    findPopularKeywordsMatching(keyword, 5),
  ]);

  const filtered = candidates
    .filter((item) => {
      const price = toEffectivePrice(item);
      if (minPrice !== null && (price === null || price < minPrice)) {
        return false;
      }
      if (maxPrice !== null && (price === null || price > maxPrice)) {
        return false;
      }
      return matchesSpecFilters(item, specFilters);
    })
    .map((item) => ({
      ...item,
      score: computeSearchScore(item, keyword, weights),
    }))
    .sort((left, right) => compareSearchItems(left, right, sort));

  const totalCount = filtered.length;
  const totalPages = totalCount === 0 ? 0 : Math.ceil(totalCount / limit);
  const offset = (page - 1) * limit;
  const hits = filtered.slice(offset, offset + limit).map((item) => ({
    id: item.id,
    name: highlightText(item.name, keyword),
    lowestPrice: toEffectivePrice(item),
    thumbnailUrl: item.thumbnailUrl,
    categoryName: item.category?.name ?? null,
    score: item.score,
  }));

  await createSearchLog({
    keyword,
    resultCount: totalCount,
    categoryId,
    filters: {
      minPrice,
      maxPrice,
      sort,
      specs: specFilters,
    },
    responseTimeMs: Date.now() - startedAt,
  });

  return {
    data: {
      hits,
      facets: buildFacetSummary(filtered),
      suggestions: collectSuggestions(keyword, filtered, synonymRows, popularRows.map((item) => ({ keyword: item.keyword }))),
      totalCount,
    },
    meta: {
      page,
      limit,
      totalPages,
    },
  };
}

export async function autocomplete(query) {
  const q = String(query.q ?? "").trim();
  if (!q) {
    throw badRequest("q is required");
  }

  const limit = normalizeLimit(query.limit, 5, MAX_AUTOCOMPLETE_LIMIT);
  const [products, categories, synonymRows, popularRows] = await Promise.all([
    findAutocompleteProducts(q, limit),
    findAutocompleteCategories(q, limit),
    findActiveSearchSynonyms(q, limit),
    findPopularKeywordsMatching(q, limit),
  ]);

  const keywords = collectSuggestions(
    q,
    products.map((item) => ({ name: item.name })),
    synonymRows,
    popularRows.map((item) => ({ keyword: item.keyword })),
  ).slice(0, limit);

  return {
    keywords,
    products: products.map((item) => ({
      id: item.id,
      name: item.name,
      thumbnailUrl: item.thumbnailUrl,
      lowestPrice: item.lowestPrice ?? null,
    })),
    categories: categories.map((item) => ({
      id: item.id,
      name: item.parent ? `${item.parent.name} > ${item.name}` : item.name,
    })),
  };
}

export async function getPopularKeywords(query) {
  const limit = normalizeLimit(query.limit, 10, 20);
  const rows = await findPopularSearchLogs(limit);

  return rows.map((item, index) => ({
    rank: index + 1,
    keyword: item.keyword,
    count: item._count.keyword,
  }));
}

export async function saveRecentKeyword(userId, payload) {
  const user = await ensureUser(userId);
  if (!user.searchHistoryEnabled) {
    throw badRequest("Search history auto save is disabled");
  }

  const keyword = String(payload?.keyword ?? "").trim();
  if (!keyword) {
    throw badRequest("keyword is required");
  }

  const existing = await findRecentSearchKeywordByUser(userId, keyword);
  if (existing) {
    await updateRecentSearchKeyword(existing.id, {
      searchedAt: new Date(),
    });
  } else {
    await createRecentSearchKeyword({
      userId: Number(userId),
      keyword,
    });
  }

  const items = await findRecentSearchKeywords(userId, DEFAULT_RECENT_LIMIT);
  await deleteOverflowRecentSearchKeywords(
    userId,
    items.map((item) => item.id),
  );

  return findRecentSearchKeywords(userId, DEFAULT_RECENT_LIMIT);
}

export async function getRecentKeywords(userId) {
  await ensureUser(userId);
  return findRecentSearchKeywords(userId, DEFAULT_RECENT_LIMIT);
}

export async function removeRecentKeyword(userId, keywordId) {
  const item = await findRecentSearchKeywordById(userId, keywordId);
  if (!item) {
    throw notFound("Recent keyword not found");
  }

  await deleteRecentSearchKeyword(item.id);
  return { message: "Recent search keyword deleted" };
}

export async function clearRecentKeywords(userId) {
  await clearRecentSearchKeywords(userId);
  return { message: "Recent search keywords cleared" };
}

export async function updateSearchPreferences(userId, payload) {
  await ensureUser(userId);

  const saveRecentSearches = Boolean(payload?.saveRecentSearches);
  await updateUser(userId, {
    searchHistoryEnabled: saveRecentSearches,
  });

  return {
    saveRecentSearches,
  };
}

export async function getSearchWeights() {
  return getWeightConfig();
}

export async function updateSearchWeights(payload) {
  const current = await getWeightConfig();
  const next = {
    nameWeight: payload?.nameWeight !== undefined ? Number(payload.nameWeight) : current.nameWeight,
    keywordWeight: payload?.keywordWeight !== undefined ? Number(payload.keywordWeight) : current.keywordWeight,
    clickWeight: payload?.clickWeight !== undefined ? Number(payload.clickWeight) : current.clickWeight,
  };

  await Promise.all(
    Object.entries(next).map(([field, weight]) => upsertSearchWeightSetting(field, weight)),
  );

  return next;
}

export async function getSearchIndexStatus() {
  const [documentCount, totalOutbox, pending, failed, completed] = await Promise.all([
    countProductQueryViews(),
    countSearchIndexOutbox(),
    countSearchIndexOutboxByStatus("PENDING"),
    countSearchIndexOutboxByStatus("FAILED"),
    countSearchIndexOutboxByStatus("COMPLETED"),
  ]);

  return {
    index: "product_query_views",
    exists: documentCount > 0,
    documentCount,
    outbox: {
      total: totalOutbox,
      pending,
      failed,
      completed,
    },
  };
}

export async function reindexAllProducts() {
  const products = await findProductsForSearchIndex();

  for (const item of products) {
    await upsertProductQueryView(toQueryViewPayload(item));
    await writeCompletedOutbox(item.id, {
      scope: "all",
      source: "manual-reindex",
    });
  }

  return {
    message: "전체 재색인 작업이 큐에 등록되었습니다.",
    queued: true,
    indexedCount: products.length,
  };
}

export async function reindexProduct(productId) {
  const product = await findProductForSearchIndex(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  await upsertProductQueryView(toQueryViewPayload(product));
  await writeCompletedOutbox(product.id, {
    scope: "single",
    source: "manual-reindex",
  });

  return {
    message: "단일 상품 재색인 작업이 큐에 등록되었습니다.",
    productId: Number(productId),
  };
}

export async function getSearchOutboxSummary() {
  const [total, pending, failed, completed] = await Promise.all([
    countSearchIndexOutbox(),
    countSearchIndexOutboxByStatus("PENDING"),
    countSearchIndexOutboxByStatus("FAILED"),
    countSearchIndexOutboxByStatus("COMPLETED"),
  ]);

  return {
    total,
    pending,
    failed,
    completed,
  };
}

export async function requeueFailedSearchOutbox(query) {
  const limit = normalizeLimit(query.limit, 10, 100);
  const failedItems = await findFailedSearchIndexOutbox(limit);

  if (failedItems.length === 0) {
    return { requeuedCount: 0 };
  }

  const result = await requeueFailedSearchIndexOutbox(failedItems.map((item) => item.id));
  return { requeuedCount: result.count };
}
