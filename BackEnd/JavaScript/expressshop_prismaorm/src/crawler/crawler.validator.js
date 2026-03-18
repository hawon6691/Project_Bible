function hasAnyOwnValue(payload, keys) {
  return keys.some((key) => payload?.[key] !== undefined);
}

export function validateCreateCrawlerJob(req) {
  const { sellerId, name } = req.body ?? {};
  return sellerId && name ? null : "sellerId and name are required";
}

export function validateUpdateCrawlerJob(req) {
  return hasAnyOwnValue(req.body, [
    "sellerId",
    "name",
    "cronExpression",
    "collectPrice",
    "collectSpec",
    "detectAnomaly",
    "isActive",
  ])
    ? null
    : "At least one field is required";
}

export function validateTriggerCrawler(req) {
  const { sellerId, collectPrice, collectSpec } = req.body ?? {};
  if (!sellerId) return "sellerId is required";
  if (collectPrice === false && collectSpec === false) {
    return "collectPrice or collectSpec must be true";
  }
  return null;
}
