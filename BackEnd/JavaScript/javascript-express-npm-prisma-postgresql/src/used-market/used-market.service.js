import { forbidden, notFound } from "../utils/http-error.js";
import {
  findBuildParts,
  findCategoryById,
  findCategoryProducts,
  findPcBuildById,
  findProductById,
  findUsedPricesByProduct,
  findUsedPricesByProductIds,
} from "./used-market.repository.js";

function getBasePrice(product) {
  return product?.lowestPrice ?? product?.discountPrice ?? product?.price ?? 0;
}

function deriveUsedPrice(product) {
  const basePrice = getBasePrice(product);
  return {
    averagePrice: Math.round(basePrice * 0.7),
    minPrice: Math.round(basePrice * 0.55),
    maxPrice: Math.round(basePrice * 0.85),
    sampleCount: 0,
    source: "derived",
    collectedAt: null,
  };
}

function resolveTrend(latest, previous) {
  if (!latest || !previous) {
    return "STABLE";
  }
  if (latest.averagePrice > previous.averagePrice) return "UP";
  if (latest.averagePrice < previous.averagePrice) return "DOWN";
  return "STABLE";
}

function getDepreciationRate(partType) {
  switch (partType) {
    case "GPU":
      return 0.6;
    case "CPU":
    case "MOTHERBOARD":
    case "MONITOR":
      return 0.65;
    case "RAM":
    case "SSD":
    case "HDD":
      return 0.7;
    default:
      return 0.5;
  }
}

export async function getProductUsedPrice(productId) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const usedPrices = await findUsedPricesByProduct(productId, 2);
  const latest = usedPrices[0] ?? deriveUsedPrice(product);
  const previous = usedPrices[1] ?? null;

  return {
    productId: product.id,
    averagePrice: latest.averagePrice,
    minPrice: latest.minPrice,
    maxPrice: latest.maxPrice,
    trend: resolveTrend(latest, previous),
    sampleCount: latest.sampleCount,
    source: latest.source,
    collectedAt: latest.collectedAt,
  };
}

export async function getCategoryUsedPrices(categoryId, query) {
  const category = await findCategoryById(categoryId);
  if (!category) {
    throw notFound("Category not found");
  }

  const page = Math.max(Number(query?.page ?? 1), 1);
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);

  const [products, total] = await findCategoryProducts(categoryId, page, limit);
  const usedPrices = await findUsedPricesByProductIds(products.map((item) => item.id));
  const grouped = new Map();

  for (const item of usedPrices) {
    const bucket = grouped.get(item.productId) ?? [];
    bucket.push(item);
    grouped.set(item.productId, bucket);
  }

  const items = products.map((product) => {
    const entries = grouped.get(product.id) ?? [];
    const latest = entries[0] ?? deriveUsedPrice(product);
    const previous = entries[1] ?? null;

    return {
      productId: product.id,
      productName: product.name,
      averagePrice: latest.averagePrice,
      minPrice: latest.minPrice,
      maxPrice: latest.maxPrice,
      trend: resolveTrend(latest, previous),
      sampleCount: latest.sampleCount,
      source: latest.source,
      collectedAt: latest.collectedAt,
    };
  });

  return {
    items,
    meta: {
      total,
      page,
      limit,
    },
  };
}

export async function estimatePcBuildUsedPrice(userId, buildId) {
  const build = await findPcBuildById(buildId);
  if (!build) {
    throw notFound("PC build not found");
  }
  if (build.userId !== Number(userId)) {
    throw forbidden("Only the owner can estimate this PC build");
  }

  const parts = await findBuildParts(buildId);
  const partBreakdown = parts.map((part) => {
    const originalPrice = part.priceAtAdd * part.quantity;
    const depreciationRate = getDepreciationRate(part.partType);
    const estimatedUsedPrice = Math.round(originalPrice * depreciationRate);

    return {
      buildPartId: part.id,
      partType: part.partType,
      productId: part.productId,
      productName: part.product?.name ?? null,
      sellerId: part.sellerId,
      sellerName: part.seller?.name ?? null,
      quantity: part.quantity,
      originalPrice,
      depreciationRate,
      estimatedUsedPrice,
    };
  });

  return {
    buildId: build.id,
    buildName: build.name,
    estimatedPrice: partBreakdown.reduce((sum, item) => sum + item.estimatedUsedPrice, 0),
    partBreakdown,
  };
}
