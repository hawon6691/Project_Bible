function isObject(value) {
  return Boolean(value) && typeof value === "object" && !Array.isArray(value);
}

export function validateCreateBadge(req) {
  const { name, description, iconUrl, type, rarity, condition } = req.body ?? {};
  if (!name || !description || !iconUrl) {
    return "name, description, iconUrl are required";
  }

  if (!type || !rarity) {
    return "type and rarity are required";
  }

  const normalizedType = String(type).trim().toUpperCase();
  if (!["AUTO", "MANUAL"].includes(normalizedType)) {
    return "type must be one of AUTO, MANUAL";
  }

  const normalizedRarity = String(rarity).trim().toUpperCase();
  if (!["COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY"].includes(normalizedRarity)) {
    return "rarity must be one of COMMON, UNCOMMON, RARE, EPIC, LEGENDARY";
  }

  if (normalizedType === "AUTO" && !isObject(condition)) {
    return "AUTO badge requires condition";
  }

  if (condition !== undefined && condition !== null && !isObject(condition)) {
    return "condition must be object";
  }

  return null;
}

export function validateUpdateBadge(req) {
  const body = req.body ?? {};
  const keys = ["name", "description", "iconUrl", "type", "condition", "rarity"];
  const provided = keys.filter((key) => body[key] !== undefined);
  if (provided.length === 0) {
    return "At least one field is required";
  }

  if (body.type !== undefined) {
    const normalizedType = String(body.type).trim().toUpperCase();
    if (!["AUTO", "MANUAL"].includes(normalizedType)) {
      return "type must be one of AUTO, MANUAL";
    }
  }

  if (body.rarity !== undefined) {
    const normalizedRarity = String(body.rarity).trim().toUpperCase();
    if (!["COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY"].includes(normalizedRarity)) {
      return "rarity must be one of COMMON, UNCOMMON, RARE, EPIC, LEGENDARY";
    }
  }

  if (body.condition !== undefined && body.condition !== null && !isObject(body.condition)) {
    return "condition must be object";
  }

  return null;
}

export function validateGrantBadge(req) {
  const { userId, reason } = req.body ?? {};
  if (!userId) {
    return "userId is required";
  }

  if (reason !== undefined && reason !== null && !String(reason).trim()) {
    return "reason must not be empty";
  }

  return null;
}
