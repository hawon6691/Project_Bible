import {
  createPriceAlert as createPriceAlertRecord,
  createPriceEntry as createPriceEntryRecord,
  createProduct as createProductRecord,
  createProductImage as createProductImageRecord,
  createProductOption as createProductOptionRecord,
  createSpecDefinition as createSpecDefinitionRecord,
  deletePriceAlert as deletePriceAlertRecord,
  deletePriceEntry as deletePriceEntryRecord,
  deleteProductImage as deleteProductImageRecord,
  deleteProductOption as deleteProductOptionRecord,
  deleteSpecDefinition as deleteSpecDefinitionRecord,
  findPriceAlertById,
  findPriceAlertByUserAndProduct,
  findPriceAlertsByUser,
  findPriceEntryById,
  findPriceHistory,
  findProductById,
  findProductImageById,
  findProductImageMaxSortOrder,
  findProductByIds,
  findProductOptionById,
  findProductPrices,
  findProducts,
  findProductSpecs,
  findSpecDefinitionById,
  findSpecDefinitions,
  findSpecScoresByDefinitionIds,
  recalculateProductPricing,
  replaceProductSpecs,
  replaceSpecScores,
  softDeleteProduct,
  updateProductImages,
  updatePriceEntry as updatePriceEntryRecord,
  updateProduct as updateProductRecord,
  updateProductOption as updateProductOptionRecord,
  updateSpecDefinition as updateSpecDefinitionRecord,
} from "./product.repository.js";
import { conflict, notFound, badRequest } from "../utils/http-error.js";
import { uploadImageWithCategory } from "../image/image.service.js";
import { deleteImageAssetWithVariants } from "../image/image.repository.js";

const SPEC_TYPES = new Set(["TEXT", "NUMBER", "SELECT"]);
const SPEC_DATA_TYPES = new Set(["STRING", "NUMBER", "BOOLEAN"]);
const PRODUCT_STATUSES = new Set(["ON_SALE", "SOLD_OUT", "HIDDEN"]);
const SHIPPING_TYPES = new Set(["FREE", "PAID", "CONDITIONAL"]);

function normalizeProductSort(sort) {
  switch (sort) {
    case "price_asc":
      return [{ lowestPrice: "asc" }, { id: "desc" }];
    case "price_desc":
      return [{ lowestPrice: "desc" }, { id: "desc" }];
    case "rating_asc":
      return [{ averageRating: "asc" }, { reviewCount: "asc" }];
    case "rating_desc":
      return [{ averageRating: "desc" }, { reviewCount: "desc" }];
    case "popularity":
      return [{ popularityScore: "desc" }, { id: "desc" }];
    case "newest":
    default:
      return [{ id: "desc" }];
  }
}

function normalizeNumericValue(value) {
  if (value === undefined || value === null || value === "") {
    return null;
  }
  return Number(value);
}

function normalizeSpecDisplayValue(spec) {
  if (!spec) return null;
  if (spec.value) return spec.value;
  if (spec.numericValue !== null && spec.numericValue !== undefined) {
    const unit = spec.specDefinition?.unit ? `${spec.specDefinition.unit}` : "";
    return `${spec.numericValue}${unit}`;
  }
  return null;
}

export async function getProducts(query) {
  const page = Number(query.page ?? 1);
  const limit = Math.min(Number(query.limit ?? 20), 100);
  const categoryId = query.categoryId ? Number(query.categoryId) : undefined;
  const search = String(query.search ?? "").trim();
  const minPrice = query.minPrice ? Number(query.minPrice) : undefined;
  const maxPrice = query.maxPrice ? Number(query.maxPrice) : undefined;
  const orderBy = normalizeProductSort(String(query.sort ?? "newest"));
  const where = {
    deletedAt: null,
    ...(categoryId ? { categoryId } : {}),
    ...(minPrice !== undefined ? { price: { gte: minPrice } } : {}),
    ...(maxPrice !== undefined ? { price: { ...(minPrice !== undefined ? { gte: minPrice } : {}), lte: maxPrice } } : {}),
    ...(search
      ? {
          OR: [
            { name: { contains: search, mode: "insensitive" } },
            { description: { contains: search, mode: "insensitive" } },
          ],
        }
      : {}),
  };

  const [items, total] = await findProducts(where, page, limit, orderBy);
  return { items, meta: { page, limit, total } };
}

export async function getProduct(productId) {
  const item = await findProductById(productId);
  if (!item) {
    throw notFound("Product not found");
  }
  return item;
}

export async function createAdminProduct(payload) {
  const { name, description, price, categoryId } = payload ?? {};
  if (!name || !description || price === undefined || !categoryId) {
    throw badRequest("name, description, price, categoryId are required");
  }

  const product = await createProductRecord({
    name,
    description,
    price: Number(price),
    discountPrice: payload?.discountPrice !== undefined ? Number(payload.discountPrice) : null,
    stock: payload?.stock !== undefined ? Number(payload.stock) : 0,
    status: PRODUCT_STATUSES.has(String(payload?.status ?? "").toUpperCase())
      ? String(payload.status).toUpperCase()
      : "ON_SALE",
    categoryId: Number(categoryId),
    thumbnailUrl: payload?.thumbnailUrl ?? null,
  });

  return getProduct(product.id);
}

export async function updateAdminProduct(productId, payload) {
  const existing = await findProductById(productId);
  if (!existing) {
    throw notFound("Product not found");
  }

  const data = {};
  if (payload?.name !== undefined) data.name = payload.name;
  if (payload?.description !== undefined) data.description = payload.description;
  if (payload?.price !== undefined) data.price = Number(payload.price);
  if (payload?.discountPrice !== undefined) data.discountPrice = payload.discountPrice === null ? null : Number(payload.discountPrice);
  if (payload?.stock !== undefined) data.stock = Number(payload.stock);
  if (payload?.categoryId !== undefined) data.categoryId = Number(payload.categoryId);
  if (payload?.thumbnailUrl !== undefined) data.thumbnailUrl = payload.thumbnailUrl;
  if (payload?.status !== undefined) {
    const status = String(payload.status).toUpperCase();
    if (!PRODUCT_STATUSES.has(status)) {
      throw badRequest("status must be ON_SALE, SOLD_OUT, or HIDDEN");
    }
    data.status = status;
  }

  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  await updateProductRecord(productId, data);
  return getProduct(productId);
}

export async function deleteAdminProduct(productId) {
  const existing = await findProductById(productId);
  if (!existing) {
    throw notFound("Product not found");
  }

  await softDeleteProduct(productId);
  return { message: "Product deleted" };
}

export async function addAdminProductImage(productId, uploaderUserId, file, payload) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const uploaded = await uploadImageWithCategory(uploaderUserId, file, "product");
  const largeVariant = uploaded.variants.find((item) => item.type === "LARGE") ?? uploaded.variants[0] ?? null;
  const isMain = payload?.isMain === true || String(payload?.isMain ?? "").toLowerCase() === "true";
  const sortAggregate = await findProductImageMaxSortOrder(productId);
  const sortOrder = (sortAggregate._max.sortOrder ?? -1) + 1;

  if (isMain) {
    await updateProductImages(productId, {}, { isMain: false });
  }

  const created = await createProductImageRecord({
    productId: Number(productId),
    url: largeVariant?.url ?? uploaded.originalUrl,
    isMain,
    sortOrder,
    imageVariantId: largeVariant?.id ?? null,
  });

  return created;
}

export async function deleteAdminProductImage(productId, imageId) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const image = await findProductImageById(imageId);
  if (!image || image.productId !== Number(productId)) {
    throw notFound("Product image not found");
  }

  await deleteProductImageRecord(imageId);

  if (image.imageVariant?.imageId) {
    try {
      await deleteImageAssetWithVariants(image.imageVariant.imageId);
    } catch {
      // Keep product image deletion successful even if the backing asset was already removed.
    }
  }

  return { message: "Product image deleted" };
}

export async function createAdminProductOption(productId, payload) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const { name, values } = payload ?? {};
  if (!name || !Array.isArray(values) || values.length === 0) {
    throw badRequest("name and values are required");
  }

  return createProductOptionRecord({
    productId: Number(productId),
    name,
    values,
  });
}

export async function updateAdminProductOption(productId, optionId, payload) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const option = await findProductOptionById(optionId);
  if (!option || option.productId !== Number(productId)) {
    throw notFound("Product option not found");
  }

  const data = {};
  if (payload?.name !== undefined) data.name = payload.name;
  if (payload?.values !== undefined) data.values = payload.values;
  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  return updateProductOptionRecord(optionId, data);
}

export async function deleteAdminProductOption(productId, optionId) {
  const option = await findProductOptionById(optionId);
  if (!option || option.productId !== Number(productId)) {
    throw notFound("Product option not found");
  }

  await deleteProductOptionRecord(optionId);
  return { message: "Product option deleted" };
}

export async function getSpecDefinitions(query) {
  const items = await findSpecDefinitions(query.categoryId);
  return { items, meta: { total: items.length } };
}

export async function createAdminSpecDefinition(payload) {
  const { categoryId, name, type } = payload ?? {};
  if (!categoryId || !name || !type) {
    throw badRequest("categoryId, name, type are required");
  }

  const normalizedType = String(type).toUpperCase();
  if (!SPEC_TYPES.has(normalizedType)) {
    throw badRequest("type must be TEXT, NUMBER, or SELECT");
  }

  const dataType = payload?.dataType ? String(payload.dataType).toUpperCase() : "STRING";
  if (!SPEC_DATA_TYPES.has(dataType)) {
    throw badRequest("dataType must be STRING, NUMBER, or BOOLEAN");
  }

  return createSpecDefinitionRecord({
    categoryId: Number(categoryId),
    name,
    type: normalizedType,
    options: Array.isArray(payload?.options) ? payload.options : payload?.options ?? null,
    unit: payload?.unit ?? null,
    isComparable: payload?.isComparable ?? true,
    dataType,
    sortOrder: payload?.sortOrder !== undefined ? Number(payload.sortOrder) : 0,
  });
}

export async function updateAdminSpecDefinition(specDefinitionId, payload) {
  const existing = await findSpecDefinitionById(specDefinitionId);
  if (!existing) {
    throw notFound("Spec definition not found");
  }

  const data = {};
  if (payload?.categoryId !== undefined) data.categoryId = Number(payload.categoryId);
  if (payload?.name !== undefined) data.name = payload.name;
  if (payload?.type !== undefined) {
    const type = String(payload.type).toUpperCase();
    if (!SPEC_TYPES.has(type)) throw badRequest("type must be TEXT, NUMBER, or SELECT");
    data.type = type;
  }
  if (payload?.options !== undefined) data.options = payload.options;
  if (payload?.unit !== undefined) data.unit = payload.unit;
  if (payload?.isComparable !== undefined) data.isComparable = Boolean(payload.isComparable);
  if (payload?.dataType !== undefined) {
    const dataType = String(payload.dataType).toUpperCase();
    if (!SPEC_DATA_TYPES.has(dataType)) throw badRequest("dataType must be STRING, NUMBER, or BOOLEAN");
    data.dataType = dataType;
  }
  if (payload?.sortOrder !== undefined) data.sortOrder = Number(payload.sortOrder);
  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  return updateSpecDefinitionRecord(specDefinitionId, data);
}

export async function deleteAdminSpecDefinition(specDefinitionId) {
  const existing = await findSpecDefinitionById(specDefinitionId);
  if (!existing) {
    throw notFound("Spec definition not found");
  }

  await deleteSpecDefinitionRecord(specDefinitionId);
  return { message: "Spec definition deleted" };
}

export async function getProductSpecs(productId) {
  const items = await findProductSpecs(productId);
  return { items, meta: { total: items.length } };
}

export async function setProductSpecs(productId, payload) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const specs = Array.isArray(payload) ? payload : payload?.specs;
  if (!Array.isArray(specs) || specs.length === 0) {
    throw badRequest("specs array is required");
  }

  const normalized = specs.map((item) => {
    if (!item?.specDefinitionId || item?.value === undefined) {
      throw badRequest("Each spec requires specDefinitionId and value");
    }

    return {
      specDefinitionId: Number(item.specDefinitionId),
      value: String(item.value),
      numericValue: normalizeNumericValue(item.numericValue),
    };
  });

  const items = await replaceProductSpecs(productId, normalized);
  return { items, meta: { total: items.length } };
}

export async function compareSpecs(payload) {
  const productIds = Array.isArray(payload?.productIds)
    ? [...new Set(payload.productIds.map((item) => Number(item)).filter(Boolean))]
    : [];
  if (productIds.length < 2 || productIds.length > 4) {
    throw badRequest("productIds must contain between 2 and 4 items");
  }

  const products = await findProductByIds(productIds);
  if (products.length !== productIds.length) {
    throw notFound("One or more products were not found");
  }

  const specsByDefinition = new Map();
  for (const product of products) {
    for (const spec of product.specs) {
      const key = spec.specDefinitionId;
      if (!specsByDefinition.has(key)) {
        specsByDefinition.set(key, {
          specDefinitionId: key,
          name: spec.specDefinition.name,
          sortOrder: spec.specDefinition.sortOrder ?? 0,
          values: new Map(),
        });
      }
      specsByDefinition.get(key).values.set(product.id, normalizeSpecDisplayValue(spec));
    }
  }

  const specs = [...specsByDefinition.values()]
    .sort((a, b) => a.sortOrder - b.sortOrder || a.name.localeCompare(b.name))
    .map((spec) => ({
      specDefinitionId: spec.specDefinitionId,
      name: spec.name,
      values: products.map((product) => spec.values.get(product.id) ?? null),
    }));

  return {
    products: products.map((item) => ({
      id: item.id,
      name: item.name,
      thumbnailUrl: item.thumbnailUrl,
      lowestPrice: item.lowestPrice,
    })),
    specs,
  };
}

export async function compareScoredSpecs(payload) {
  const compared = await compareSpecs(payload);
  const specDefinitionIds = compared.specs.map((item) => item.specDefinitionId);
  const scoreMappings = await findSpecScoresByDefinitionIds(specDefinitionIds);
  const scoreMap = new Map();

  for (const item of scoreMappings) {
    const key = `${item.specDefinitionId}:${item.value}`;
    scoreMap.set(key, item.score);
  }

  const defaultWeight = compared.specs.length > 0 ? Math.floor(100 / compared.specs.length) : 0;
  const weights = payload?.weights ?? {};

  const totals = compared.products.map((product) => ({
    id: product.id,
    name: product.name,
    totalScore: 0,
  }));

  const specScores = compared.specs.map((spec) => {
    const scores = spec.values.map((value) =>
      value === null ? null : scoreMap.get(`${spec.specDefinitionId}:${value}`) ?? null,
    );

    const weight = Number(weights?.[spec.name] ?? defaultWeight);
    scores.forEach((score, index) => {
      if (score !== null) {
        totals[index].totalScore += (score * weight) / 100;
      }
    });

    const maxScore = Math.max(...scores.filter((score) => score !== null), -Infinity);
    const winner = Number.isFinite(maxScore) ? scores.findIndex((score) => score === maxScore) + 1 : null;

    return {
      name: spec.name,
      scores,
      winner,
    };
  });

  const rankedProducts = totals
    .map((item) => ({
      ...item,
      totalScore: Math.round(item.totalScore),
    }))
    .sort((a, b) => b.totalScore - a.totalScore || a.name.localeCompare(b.name))
    .map((item, index) => ({
      id: item.id,
      name: item.name,
      totalScore: item.totalScore,
      rank: index + 1,
    }));

  const top = rankedProducts[0];

  return {
    products: rankedProducts,
    specScores,
    recommendation: top ? `${top.name} has the highest overall score.` : null,
  };
}

export async function updateSpecScores(specDefinitionId, payload) {
  const specDefinition = await findSpecDefinitionById(specDefinitionId);
  if (!specDefinition) {
    throw notFound("Spec definition not found");
  }

  const scores = Array.isArray(payload) ? payload : payload?.scores;
  if (!Array.isArray(scores)) {
    throw badRequest("scores array is required");
  }

  return replaceSpecScores(specDefinitionId, scores);
}

export async function getProductPrices(productId) {
  const items = await findProductPrices(productId);
  return { items, meta: { total: items.length } };
}

export async function createProductPrice(productId, payload) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const { sellerId, price, productUrl } = payload ?? {};
  if (!sellerId || price === undefined || !productUrl) {
    throw badRequest("sellerId, price, productUrl are required");
  }

  const shippingType = payload?.shippingType ? String(payload.shippingType).toUpperCase() : "PAID";
  if (!SHIPPING_TYPES.has(shippingType)) {
    throw badRequest("shippingType must be FREE, PAID, or CONDITIONAL");
  }

  const created = await createPriceEntryRecord({
    productId: Number(productId),
    sellerId: Number(sellerId),
    price: Number(price),
    shippingCost: payload?.shippingCost !== undefined ? Number(payload.shippingCost) : 0,
    shippingInfo: payload?.shippingInfo ?? null,
    productUrl,
    shippingFee: payload?.shippingFee !== undefined ? Number(payload.shippingFee) : 0,
    shippingType,
    crawledAt: payload?.crawledAt ? new Date(payload.crawledAt) : null,
    isAvailable: payload?.isAvailable ?? true,
  });

  await recalculateProductPricing(productId);
  return findPriceEntryById(created.id);
}

export async function updateProductPrice(priceEntryId, payload) {
  const existing = await findPriceEntryById(priceEntryId);
  if (!existing) {
    throw notFound("Price entry not found");
  }

  const data = {};
  if (payload?.sellerId !== undefined) data.sellerId = Number(payload.sellerId);
  if (payload?.price !== undefined) data.price = Number(payload.price);
  if (payload?.shippingCost !== undefined) data.shippingCost = Number(payload.shippingCost);
  if (payload?.shippingInfo !== undefined) data.shippingInfo = payload.shippingInfo;
  if (payload?.productUrl !== undefined) data.productUrl = payload.productUrl;
  if (payload?.shippingFee !== undefined) data.shippingFee = Number(payload.shippingFee);
  if (payload?.shippingType !== undefined) {
    const shippingType = String(payload.shippingType).toUpperCase();
    if (!SHIPPING_TYPES.has(shippingType)) {
      throw badRequest("shippingType must be FREE, PAID, or CONDITIONAL");
    }
    data.shippingType = shippingType;
  }
  if (payload?.crawledAt !== undefined) data.crawledAt = payload.crawledAt ? new Date(payload.crawledAt) : null;
  if (payload?.isAvailable !== undefined) data.isAvailable = Boolean(payload.isAvailable);

  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  await updatePriceEntryRecord(priceEntryId, data);
  await recalculateProductPricing(existing.productId);
  return findPriceEntryById(priceEntryId);
}

export async function deleteProductPrice(priceEntryId) {
  const existing = await findPriceEntryById(priceEntryId);
  if (!existing) {
    throw notFound("Price entry not found");
  }

  await deletePriceEntryRecord(priceEntryId);
  await recalculateProductPricing(existing.productId);
  return { message: "Price entry deleted" };
}

export async function getPriceHistory(productId) {
  const items = await findPriceHistory(productId);
  return { items, meta: { total: items.length } };
}

export async function getPriceAlerts(userId) {
  const items = await findPriceAlertsByUser(userId);
  return { items, meta: { total: items.length } };
}

export async function createPriceAlert(userId, payload) {
  const { productId, targetPrice } = payload ?? {};
  if (!productId || targetPrice === undefined) {
    throw badRequest("productId and targetPrice are required");
  }

  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const existing = await findPriceAlertByUserAndProduct(userId, productId);
  if (existing) {
    throw conflict("Price alert already exists");
  }

  return createPriceAlertRecord({
    userId,
    productId: Number(productId),
    targetPrice: Number(targetPrice),
  });
}

export async function deletePriceAlert(userId, priceAlertId) {
  const existing = await findPriceAlertById(priceAlertId);
  if (!existing || existing.userId !== Number(userId) || !existing.isActive) {
    throw notFound("Price alert not found");
  }

  await deletePriceAlertRecord(priceAlertId);
  return { message: "Price alert deleted" };
}
