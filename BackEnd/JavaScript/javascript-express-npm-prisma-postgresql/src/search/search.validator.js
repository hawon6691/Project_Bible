function isPositiveInteger(value) {
  if (value === undefined || value === null || value === "") {
    return false;
  }
  return Number.isInteger(Number(value)) && Number(value) > 0;
}

function isNumberLike(value) {
  if (value === undefined || value === null || value === "") {
    return false;
  }
  return Number.isFinite(Number(value));
}

export function validateSearchQuery(req) {
  const q = String(req.query?.q ?? "").trim();
  if (!q) {
    return "q is required";
  }

  if (req.query?.page !== undefined && !isPositiveInteger(req.query.page)) {
    return "page must be a positive integer";
  }

  if (req.query?.limit !== undefined && !isPositiveInteger(req.query.limit)) {
    return "limit must be a positive integer";
  }

  if (req.query?.categoryId !== undefined && !isPositiveInteger(req.query.categoryId)) {
    return "categoryId must be a positive integer";
  }

  if (req.query?.minPrice !== undefined && !isNumberLike(req.query.minPrice)) {
    return "minPrice must be numeric";
  }

  if (req.query?.maxPrice !== undefined && !isNumberLike(req.query.maxPrice)) {
    return "maxPrice must be numeric";
  }

  return null;
}

export function validateSearchAutocompleteQuery(req) {
  const q = String(req.query?.q ?? "").trim();
  if (!q) {
    return "q is required";
  }

  if (req.query?.limit !== undefined && !isPositiveInteger(req.query.limit)) {
    return "limit must be a positive integer";
  }

  return null;
}

export function validatePopularSearchQuery(req) {
  if (req.query?.limit !== undefined && !isPositiveInteger(req.query.limit)) {
    return "limit must be a positive integer";
  }

  return null;
}

export function validateSaveRecentSearch(req) {
  const keyword = String(req.body?.keyword ?? "").trim();
  return keyword ? null : "keyword is required";
}

export function validateUpdateSearchPreference(req) {
  return typeof req.body?.saveRecentSearches === "boolean"
    ? null
    : "saveRecentSearches must be a boolean";
}

export function validateUpdateSearchWeights(req) {
  const body = req.body ?? {};
  return body.nameWeight !== undefined || body.keywordWeight !== undefined || body.clickWeight !== undefined
    ? null
    : "At least one weight field is required";
}
