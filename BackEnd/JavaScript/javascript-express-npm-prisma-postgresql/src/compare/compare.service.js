import {
  createCompareItem,
  deleteCompareItem,
  findCompareItem,
  findCompareItems,
  findProductById,
} from "./compare.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

const MAX_COMPARE_ITEMS = 4;

function normalizeCompareKey(compareKey) {
  const key = String(compareKey ?? "").trim();
  return key ? key.slice(0, 100) : "guest";
}

function normalizeCompareList(items) {
  return items
    .filter((item) => item.product)
    .map((item) => ({
      productId: item.product.id,
      name: item.product.name,
      categoryId: item.product.categoryId,
      price: item.product.lowestPrice ?? item.product.discountPrice ?? item.product.price,
      averageRating: Number(item.product.averageRating ?? 0),
      reviewCount: item.product.reviewCount,
      sellerCount: item.product.sellerCount,
      salesCount: item.product.salesCount,
      thumbnailUrl: item.product.thumbnailUrl,
    }));
}

function isDifferent(values) {
  const normalized = values.filter((value) => value !== null && value !== undefined);
  if (normalized.length <= 1) return false;
  return new Set(normalized).size > 1;
}

export async function addCompareItem(compareKey, payload) {
  const productId = Number(payload?.productId);
  if (!payload?.productId || Number.isNaN(productId)) {
    throw badRequest("productId is required");
  }

  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const normalizedKey = normalizeCompareKey(compareKey);
  const existing = await findCompareItem(normalizedKey, productId);
  if (!existing) {
    const currentItems = await findCompareItems(normalizedKey);
    if (currentItems.length >= MAX_COMPARE_ITEMS) {
      throw badRequest(`Compare list can contain up to ${MAX_COMPARE_ITEMS} items`);
    }

    const sortOrder = currentItems.length === 0 ? 1 : currentItems[currentItems.length - 1].sortOrder + 1;
    await createCompareItem({
      compareKey: normalizedKey,
      productId,
      sortOrder,
    });
  }

  return getCompareList(normalizedKey);
}

export async function removeCompareItem(compareKey, productId) {
  const normalizedKey = normalizeCompareKey(compareKey);
  await deleteCompareItem(normalizedKey, productId);
  return getCompareList(normalizedKey);
}

export async function getCompareList(compareKey) {
  const normalizedKey = normalizeCompareKey(compareKey);
  const items = await findCompareItems(normalizedKey);
  return {
    compareKey: normalizedKey,
    compareList: normalizeCompareList(items),
  };
}

export async function getCompareDetail(compareKey) {
  const normalizedKey = normalizeCompareKey(compareKey);
  const items = normalizeCompareList(await findCompareItems(normalizedKey));

  return {
    compareKey: normalizedKey,
    items,
    differences: {
      price: isDifferent(items.map((item) => item.price)),
      categoryId: isDifferent(items.map((item) => item.categoryId)),
      averageRating: isDifferent(items.map((item) => item.averageRating)),
      reviewCount: isDifferent(items.map((item) => item.reviewCount)),
      sellerCount: isDifferent(items.map((item) => item.sellerCount)),
      salesCount: isDifferent(items.map((item) => item.salesCount)),
    },
  };
}
