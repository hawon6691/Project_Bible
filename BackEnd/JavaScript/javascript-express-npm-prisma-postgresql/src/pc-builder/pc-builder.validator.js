import { validateCompatibilitySeverity, validatePartType } from "./pc-builder.service.js";

const BUILD_PURPOSES = ["GAMING", "OFFICE", "DESIGN", "DEVELOPMENT", "STREAMING"];

function validatePageLimit(query) {
  for (const key of ["page", "limit"]) {
    if (query?.[key] === undefined) {
      continue;
    }

    const parsed = Number(query[key]);
    if (!Number.isInteger(parsed) || parsed <= 0) {
      return `${key} must be a positive integer`;
    }
  }

  return null;
}

export function validatePcBuildListQuery(req) {
  return validatePageLimit(req.query);
}

export function validateCreatePcBuild(req) {
  const { name, purpose, budget } = req.body ?? {};

  if (typeof name !== "string" || name.trim() === "") {
    return "name is required";
  }
  if (!BUILD_PURPOSES.includes(purpose)) {
    return "purpose is invalid";
  }
  if (req.body?.description !== undefined && req.body.description !== null && typeof req.body.description !== "string") {
    return "description must be a string or null";
  }
  if (budget !== undefined && (!Number.isInteger(Number(budget)) || Number(budget) < 0)) {
    return "budget must be a non-negative integer";
  }

  return null;
}

export function validateUpdatePcBuild(req) {
  const { name, purpose, budget } = req.body ?? {};

  if (name !== undefined && (typeof name !== "string" || name.trim() === "")) {
    return "name must not be empty";
  }
  if (purpose !== undefined && !BUILD_PURPOSES.includes(purpose)) {
    return "purpose is invalid";
  }
  if (req.body?.description !== undefined && req.body.description !== null && typeof req.body.description !== "string") {
    return "description must be a string or null";
  }
  if (budget !== undefined && (!Number.isInteger(Number(budget)) || Number(budget) < 0)) {
    return "budget must be a non-negative integer";
  }

  return null;
}

export function validateAddPcBuildPart(req) {
  const { productId, partType, quantity, sellerId } = req.body ?? {};

  if (!Number.isInteger(Number(productId)) || Number(productId) <= 0) {
    return "productId must be a positive integer";
  }
  if (!validatePartType(partType)) {
    return "partType is invalid";
  }
  if (quantity !== undefined && (!Number.isInteger(Number(quantity)) || Number(quantity) <= 0)) {
    return "quantity must be a positive integer";
  }
  if (sellerId !== undefined && (!Number.isInteger(Number(sellerId)) || Number(sellerId) <= 0)) {
    return "sellerId must be a positive integer";
  }

  return null;
}

export function validateCompatibilityRulePayload(req) {
  const { partType, targetPartType, title, description, severity } = req.body ?? {};

  if (req.method === "POST") {
    if (!validatePartType(partType)) {
      return "partType is invalid";
    }
    if (typeof title !== "string" || title.trim() === "") {
      return "title is required";
    }
    if (typeof description !== "string" || description.trim() === "") {
      return "description is required";
    }
  }

  if (partType !== undefined && !validatePartType(partType)) {
    return "partType is invalid";
  }
  if (targetPartType !== undefined && targetPartType !== null && !validatePartType(targetPartType)) {
    return "targetPartType is invalid";
  }
  if (title !== undefined && (typeof title !== "string" || title.trim() === "")) {
    return "title must not be empty";
  }
  if (description !== undefined && (typeof description !== "string" || description.trim() === "")) {
    return "description must not be empty";
  }
  if (severity !== undefined && !validateCompatibilitySeverity(severity)) {
    return "severity is invalid";
  }
  if (req.body?.enabled !== undefined && typeof req.body.enabled !== "boolean") {
    return "enabled must be a boolean";
  }
  if (req.body?.metadata !== undefined && req.body.metadata !== null && typeof req.body.metadata !== "object") {
    return "metadata must be an object or null";
  }

  return null;
}
