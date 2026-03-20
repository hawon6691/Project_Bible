import { notFound } from "../utils/http-error.js";
import { findLowestPriceHistory, findProductById } from "./analytics.repository.js";

function getCurrentPrice(product) {
  return product.lowestPrice ?? product.discountPrice ?? product.price;
}

function extractQuantityUnit(text) {
  const normalized = String(text ?? "").toLowerCase();
  const patterns = [
    { regex: /(\d+(?:\.\d+)?)\s?kg\b/, unit: "kg" },
    { regex: /(\d+(?:\.\d+)?)\s?g\b/, unit: "g" },
    { regex: /(\d+(?:\.\d+)?)\s?l\b/, unit: "l" },
    { regex: /(\d+(?:\.\d+)?)\s?ml\b/, unit: "ml" },
    { regex: /(\d+(?:\.\d+)?)\s?tb\b/, unit: "tb" },
    { regex: /(\d+(?:\.\d+)?)\s?gb\b/, unit: "gb" },
    { regex: /(\d+)\s?(?:개|ea|pcs|pack)\b/, unit: "ea" },
  ];

  for (const pattern of patterns) {
    const matched = normalized.match(pattern.regex);
    if (!matched) continue;

    const quantity = Number(matched[1]);
    if (!Number.isFinite(quantity) || quantity <= 0) continue;

    return { quantity, unit: pattern.unit };
  }

  return null;
}

async function requireProduct(productId) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }
  return product;
}

export async function getLowestEver(productId) {
  const product = await requireProduct(productId);
  const currentPrice = getCurrentPrice(product);
  const lowestHistory = await findLowestPriceHistory(productId);

  if (!lowestHistory) {
    return {
      isLowestEver: true,
      currentPrice,
      lowestPrice: currentPrice,
      lowestDate: null,
    };
  }

  return {
    isLowestEver: currentPrice <= lowestHistory.lowestPrice,
    currentPrice,
    lowestPrice: lowestHistory.lowestPrice,
    lowestDate: lowestHistory.date,
  };
}

export async function getUnitPrice(productId) {
  const product = await requireProduct(productId);
  const basePrice = getCurrentPrice(product);
  const parsed = extractQuantityUnit(`${product.name} ${product.description ?? ""}`);

  if (!parsed) {
    return {
      unitPrice: basePrice,
      unit: "ea",
      quantity: 1,
    };
  }

  return {
    unitPrice: Number((basePrice / Math.max(parsed.quantity, 1)).toFixed(2)),
    unit: parsed.unit,
    quantity: parsed.quantity,
  };
}
