import {
  findPriceHistory,
  findProductById,
  findProductPrices,
  findProducts,
  findProductSpecs,
  findSpecDefinitions,
} from "./product.repository.js";
import { notFound } from "../utils/http-error.js";

export async function getProducts(query) {
  const page = Number(query.page ?? 1);
  const limit = Math.min(Number(query.limit ?? 20), 100);
  const categoryId = query.categoryId ? Number(query.categoryId) : undefined;
  const search = String(query.search ?? "").trim();
  const where = {
    ...(categoryId ? { categoryId } : {}),
    ...(search
      ? {
          OR: [
            { name: { contains: search, mode: "insensitive" } },
            { description: { contains: search, mode: "insensitive" } },
          ],
        }
      : {}),
  };

  const [items, total] = await findProducts(where, page, limit);
  return { items, meta: { page, limit, total } };
}

export async function getProduct(productId) {
  const item = await findProductById(productId);
  if (!item) {
    throw notFound("Product not found");
  }
  return item;
}

export async function getSpecDefinitions(query) {
  const items = await findSpecDefinitions(query.categoryId);
  return { items, meta: { total: items.length } };
}

export async function getProductSpecs(productId) {
  const items = await findProductSpecs(productId);
  return { items, meta: { total: items.length } };
}

export async function getProductPrices(productId) {
  const items = await findProductPrices(productId);
  return { items, meta: { total: items.length } };
}

export async function getPriceHistory(productId) {
  const items = await findPriceHistory(productId);
  return { items, meta: { total: items.length } };
}
