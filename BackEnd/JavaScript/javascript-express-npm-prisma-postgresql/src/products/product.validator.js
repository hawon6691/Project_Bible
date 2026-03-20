export function validateCreateProduct(req) {
  const { name, description, price, categoryId } = req.body ?? {};
  return name && description && price !== undefined && categoryId
    ? null
    : "name, description, price, categoryId are required";
}

export function validateUpdateProduct(req) {
  const { name, description, price, discountPrice, stock, status, categoryId, thumbnailUrl } = req.body ?? {};
  return (
    name !== undefined ||
    description !== undefined ||
    price !== undefined ||
    discountPrice !== undefined ||
    stock !== undefined ||
    status !== undefined ||
    categoryId !== undefined ||
    thumbnailUrl !== undefined
  )
    ? null
    : "At least one field is required";
}

export function validateCreateProductOption(req) {
  const { name, values } = req.body ?? {};
  return name && Array.isArray(values) && values.length > 0 ? null : "name and values are required";
}

export function validateCreateProductImage(req) {
  const { isMain } = req.body ?? {};
  if (isMain === undefined) {
    return null;
  }
  return ["true", "false"].includes(String(isMain).toLowerCase()) ? null : "isMain must be a boolean";
}

export function validateUpdateProductOption(req) {
  const { name, values } = req.body ?? {};
  return name !== undefined || values !== undefined ? null : "At least one field is required";
}

export function validateCreateSpecDefinition(req) {
  const { categoryId, name, type } = req.body ?? {};
  return categoryId && name && type ? null : "categoryId, name, type are required";
}

export function validateUpdateSpecDefinition(req) {
  const { categoryId, name, type, options, unit, isComparable, dataType, sortOrder } = req.body ?? {};
  return (
    categoryId !== undefined ||
    name !== undefined ||
    type !== undefined ||
    options !== undefined ||
    unit !== undefined ||
    isComparable !== undefined ||
    dataType !== undefined ||
    sortOrder !== undefined
  )
    ? null
    : "At least one field is required";
}

export function validateSetProductSpecs(req) {
  const specs = Array.isArray(req.body) ? req.body : req.body?.specs;
  return Array.isArray(specs) && specs.length > 0 ? null : "specs array is required";
}

export function validateCompareSpecs(req) {
  return Array.isArray(req.body?.productIds) ? null : "productIds array is required";
}

export function validateUpdateSpecScores(req) {
  const scores = Array.isArray(req.body) ? req.body : req.body?.scores;
  return Array.isArray(scores) ? null : "scores array is required";
}

export function validateCreatePriceEntry(req) {
  const { sellerId, price, productUrl } = req.body ?? {};
  return sellerId && price !== undefined && productUrl ? null : "sellerId, price, productUrl are required";
}

export function validateUpdatePriceEntry(req) {
  const { sellerId, price, shippingCost, shippingInfo, productUrl, shippingFee, shippingType, isAvailable, crawledAt } = req.body ?? {};
  return (
    sellerId !== undefined ||
    price !== undefined ||
    shippingCost !== undefined ||
    shippingInfo !== undefined ||
    productUrl !== undefined ||
    shippingFee !== undefined ||
    shippingType !== undefined ||
    isAvailable !== undefined ||
    crawledAt !== undefined
  )
    ? null
    : "At least one field is required";
}

export function validateCreatePriceAlert(req) {
  const { productId, targetPrice } = req.body ?? {};
  return productId && targetPrice !== undefined ? null : "productId and targetPrice are required";
}
