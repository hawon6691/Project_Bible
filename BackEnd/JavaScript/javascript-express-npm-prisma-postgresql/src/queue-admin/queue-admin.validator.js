function asPositiveInteger(value) {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

export function validateFailedJobsQuery(req) {
  const { page, limit, newestFirst } = req.query ?? {};
  if (page !== undefined && asPositiveInteger(page) == null) return "page must be a positive integer";
  if (limit !== undefined && asPositiveInteger(limit) == null) return "limit must be a positive integer";
  if (
    newestFirst !== undefined &&
    !["true", "false"].includes(String(newestFirst).toLowerCase())
  ) {
    return "newestFirst must be true or false";
  }
  return null;
}

export function validateRetryFailedJobsQuery(req) {
  const { limit } = req.query ?? {};
  if (limit !== undefined && asPositiveInteger(limit) == null) return "limit must be a positive integer";
  return null;
}

export function validateAutoRetryQuery(req) {
  const { perQueueLimit, maxTotal } = req.query ?? {};
  if (perQueueLimit !== undefined && asPositiveInteger(perQueueLimit) == null) {
    return "perQueueLimit must be a positive integer";
  }
  if (maxTotal !== undefined && asPositiveInteger(maxTotal) == null) {
    return "maxTotal must be a positive integer";
  }
  return null;
}
