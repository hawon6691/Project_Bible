const ALLOWED_SORTS = new Set(["newest", "price_asc", "price_desc", "popularity", "rating"]);

function isPositiveIntegerString(value) {
  if (value === undefined || value === null || value === "") {
    return true;
  }

  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0;
}

export function validateQueryProducts(req) {
  const { page, limit, categoryId, minPrice, maxPrice, sort } = req.query;

  if (!isPositiveIntegerString(page)) {
    return "page must be a positive integer";
  }

  if (!isPositiveIntegerString(limit)) {
    return "limit must be a positive integer";
  }

  if (!isPositiveIntegerString(categoryId)) {
    return "categoryId must be a positive integer";
  }

  if (!isPositiveIntegerString(minPrice)) {
    return "minPrice must be a positive integer";
  }

  if (!isPositiveIntegerString(maxPrice)) {
    return "maxPrice must be a positive integer";
  }

  if (sort !== undefined && sort !== null && sort !== "" && !ALLOWED_SORTS.has(String(sort))) {
    return `sort must be one of: ${[...ALLOWED_SORTS].join(", ")}`;
  }

  if (
    isPositiveIntegerString(minPrice) &&
    isPositiveIntegerString(maxPrice) &&
    minPrice !== undefined &&
    minPrice !== null &&
    minPrice !== "" &&
    maxPrice !== undefined &&
    maxPrice !== null &&
    maxPrice !== "" &&
    Number(minPrice) > Number(maxPrice)
  ) {
    return "minPrice must be less than or equal to maxPrice";
  }

  return null;
}
