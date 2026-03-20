import { reindexAllProducts, reindexProduct } from "../search/search.service.js";
import { badRequest, notFound } from "../utils/http-error.js";
import {
  findProductQueryViewByProductId,
  findProductQueryViews,
} from "./query.repository.js";

const DEFAULT_PAGE = 1;
const DEFAULT_LIMIT = 20;
const MAX_LIMIT = 100;
const ALLOWED_SORTS = new Set(["newest", "price_asc", "price_desc", "popularity", "rating"]);

function normalizePositiveInteger(value, fieldName, fallback = null) {
  if (value === undefined || value === null || value === "") {
    return fallback;
  }

  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw badRequest(`${fieldName} must be a positive integer`);
  }

  return parsed;
}

function normalizeSort(value) {
  if (value === undefined || value === null || value === "") {
    return "newest";
  }

  const sort = String(value);
  if (!ALLOWED_SORTS.has(sort)) {
    throw badRequest(`sort must be one of: ${[...ALLOWED_SORTS].join(", ")}`);
  }

  return sort;
}

function toEffectivePrice(item) {
  return item.lowestPrice ?? item.basePrice ?? null;
}

function compareItems(left, right, sort) {
  switch (sort) {
    case "price_asc":
      return (toEffectivePrice(left) ?? Number.MAX_SAFE_INTEGER) - (toEffectivePrice(right) ?? Number.MAX_SAFE_INTEGER) ||
        right.productId - left.productId;
    case "price_desc":
      return (toEffectivePrice(right) ?? 0) - (toEffectivePrice(left) ?? 0) ||
        right.productId - left.productId;
    case "popularity":
      return Number(right.popularityScore ?? 0) - Number(left.popularityScore ?? 0) ||
        right.viewCount - left.viewCount ||
        right.productId - left.productId;
    case "rating":
      return Number(right.averageRating ?? 0) - Number(left.averageRating ?? 0) ||
        right.reviewCount - left.reviewCount ||
        right.productId - left.productId;
    case "newest":
    default:
      return new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime() ||
        right.productId - left.productId;
  }
}

function toQueryProductDto(item) {
  return {
    productId: Number(item.productId),
    categoryId: Number(item.categoryId),
    name: item.name,
    thumbnailUrl: item.thumbnailUrl ?? null,
    status: item.status,
    basePrice: Number(item.basePrice),
    lowestPrice: item.lowestPrice === null || item.lowestPrice === undefined ? null : Number(item.lowestPrice),
    sellerCount: Number(item.sellerCount ?? 0),
    averageRating: Number(item.averageRating ?? 0),
    reviewCount: Number(item.reviewCount ?? 0),
    viewCount: Number(item.viewCount ?? 0),
    popularityScore: Number(item.popularityScore ?? 0),
    syncedAt: item.syncedAt,
    updatedAt: item.updatedAt,
  };
}

export async function findQueryProducts(query) {
  const page = normalizePositiveInteger(query.page, "page", DEFAULT_PAGE);
  const limit = Math.min(normalizePositiveInteger(query.limit, "limit", DEFAULT_LIMIT), MAX_LIMIT);
  const categoryId = normalizePositiveInteger(query.categoryId, "categoryId");
  const minPrice = normalizePositiveInteger(query.minPrice, "minPrice");
  const maxPrice = normalizePositiveInteger(query.maxPrice, "maxPrice");
  const sort = normalizeSort(query.sort);
  const keyword = String(query.keyword ?? query.q ?? "").trim();

  if (minPrice !== null && maxPrice !== null && minPrice > maxPrice) {
    throw badRequest("minPrice must be less than or equal to maxPrice");
  }

  const rows = await findProductQueryViews({
    categoryId,
    keyword,
  });

  const filtered = rows
    .filter((item) => {
      const effectivePrice = toEffectivePrice(item);

      if (minPrice !== null && (effectivePrice === null || effectivePrice < minPrice)) {
        return false;
      }

      if (maxPrice !== null && (effectivePrice === null || effectivePrice > maxPrice)) {
        return false;
      }

      return true;
    })
    .sort((left, right) => compareItems(left, right, sort));

  const total = filtered.length;
  const totalPages = total === 0 ? 0 : Math.ceil(total / limit);
  const offset = (page - 1) * limit;
  const data = filtered.slice(offset, offset + limit).map(toQueryProductDto);

  return {
    data,
    meta: {
      page,
      limit,
      total,
      totalPages,
    },
  };
}

export async function findQueryProductDetail(productId) {
  const normalizedId = normalizePositiveInteger(productId, "productId");
  const item = await findProductQueryViewByProductId(normalizedId);

  if (!item) {
    throw notFound("Product query view not found");
  }

  return toQueryProductDto(item);
}

export async function syncQueryProduct(productId) {
  const normalizedId = normalizePositiveInteger(productId, "productId");
  await reindexProduct(normalizedId);
  return findQueryProductDetail(normalizedId);
}

export async function rebuildQueryProducts() {
  const result = await reindexAllProducts();
  return {
    syncedCount: Number(result.indexedCount ?? 0),
  };
}
