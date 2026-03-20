import crypto from "crypto";

import { badRequest, conflict, notFound } from "../utils/http-error.js";
import {
  createCompatibilityRuleRecord,
  createPcBuildPartRecord,
  createPcBuildRecord,
  deleteCompatibilityRuleRecord,
  deletePcBuildPartRecord,
  deletePcBuildRecord,
  findBestPriceEntryForPcBuild,
  findCompatibilityRuleById,
  findMyPcBuilds,
  findOwnedPcBuild,
  findPcBuildById,
  findPcBuildByShareCode,
  findPcBuildPartById,
  findPcBuildPartByType,
  findPopularPcBuilds,
  findPriceEntryForPcBuild,
  findProductForPcBuild,
  incrementPcBuildViewCount,
  listCompatibilityRules,
  listEnabledCompatibilityRules,
  syncPcBuildTotalPrice,
  updateCompatibilityRuleRecord,
  updatePcBuildPartRecord,
  updatePcBuildRecord,
} from "./pc-builder.repository.js";
import { toCompatibilityRuleDto, toPcBuildDetailDto, toPcBuildSummaryDto } from "./pc-builder.mapper.js";

const REQUIRED_PARTS = ["CPU", "MOTHERBOARD", "RAM", "GPU", "SSD", "PSU", "CASE"];
const SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];
const WATTAGE_BY_PART_TYPE = {
  CPU: 125,
  MOTHERBOARD: 60,
  RAM: 15,
  GPU: 250,
  SSD: 10,
  HDD: 15,
  PSU: 0,
  CASE: 10,
  COOLER: 8,
  MONITOR: 35,
};

function normalizePage(value, fallback = 1) {
  const parsed = Number(value ?? fallback);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    return fallback;
  }
  return parsed;
}

function normalizeLimit(value, fallback = 20) {
  const parsed = Number(value ?? fallback);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.min(parsed, 100);
}

function paginate(total, page, limit) {
  return {
    total,
    page,
    limit,
    totalPages: total === 0 ? 0 : Math.ceil(total / limit),
  };
}

function buildMissingPartWarnings(missingParts) {
  return missingParts.map((partType) => ({
    type: "MISSING_PART",
    message: `${partType} is missing`,
    severity: "MEDIUM",
  }));
}

function getPowerEstimate(parts) {
  const totalWattage = parts.reduce(
    (sum, item) => sum + (WATTAGE_BY_PART_TYPE[item.partType] ?? 20) * item.quantity,
    0,
  );
  const hasPsu = parts.some((item) => item.partType === "PSU");
  const psuWattage = hasPsu ? 650 : 0;
  const headroom = psuWattage - totalWattage;

  return {
    totalWattage,
    psuWattage,
    headroom,
    sufficient: hasPsu ? headroom >= 100 : false,
  };
}

function evaluateCompatibilityRules(parts, rules) {
  const selected = new Set(parts.map((item) => item.partType));

  return rules
    .filter((rule) => selected.has(rule.partType))
    .filter((rule) => !rule.targetPartType || selected.has(rule.targetPartType))
    .map((rule) => ({
      type: "RULE",
      ruleId: rule.id,
      message: rule.description,
      title: rule.title,
      severity: rule.severity,
    }));
}

function deriveBottleneck(parts, purpose) {
  if (purpose !== "GAMING") {
    return null;
  }

  const hasCpu = parts.some((item) => item.partType === "CPU");
  const hasGpu = parts.some((item) => item.partType === "GPU");

  if (hasCpu && !hasGpu) {
    return {
      type: "BOTTLENECK",
      message: "GPU is recommended for a gaming build",
      severity: "MEDIUM",
    };
  }

  return null;
}

async function evaluateCompatibility(build) {
  const rules = await listEnabledCompatibilityRules();
  const parts = build.pcParts ?? [];
  const missingParts = REQUIRED_PARTS.filter((partType) => !parts.some((item) => item.partType === partType));
  const warnings = [...buildMissingPartWarnings(missingParts), ...evaluateCompatibilityRules(parts, rules)];
  const bottleneck = deriveBottleneck(parts, build.purpose);

  if (bottleneck) {
    warnings.push(bottleneck);
  }

  const errors = [];
  const issues = warnings.filter((item) => item.type === "RULE" && item.severity === "HIGH");
  const powerEstimate = getPowerEstimate(parts);

  if (parts.length === 0) {
    return {
      status: "EMPTY",
      issues: [],
      warnings: [],
      errors: [],
      missingParts,
      powerEstimate,
      socketCompatible: null,
      ramCompatible: null,
      formFactorCompatible: null,
      bottleneck: null,
    };
  }

  if (parts.some((item) => item.partType === "PSU") && !powerEstimate.sufficient) {
    errors.push({
      type: "POWER",
      message: "PSU headroom is too low for this build",
      severity: "HIGH",
    });
  }

  let status = "OK";
  if (errors.length > 0) {
    status = "ERROR";
  } else if (missingParts.length > 0) {
    status = "INCOMPLETE";
  } else if (warnings.length > 0) {
    status = "WARNING";
  }

  return {
    status,
    issues,
    warnings,
    errors,
    missingParts,
    powerEstimate,
    socketCompatible: issues.length === 0,
    ramCompatible: true,
    formFactorCompatible: true,
    bottleneck,
  };
}

async function getBuildOrThrow(buildId) {
  const build = await findPcBuildById(buildId);
  if (!build) {
    throw notFound("PC build not found");
  }
  return build;
}

async function getOwnedBuildOrThrow(userId, buildId) {
  const build = await findOwnedPcBuild(userId, buildId);
  if (!build) {
    throw notFound("PC build not found");
  }
  return build;
}

async function toBuildDetail(build) {
  const compatibility = await evaluateCompatibility(build);
  return toPcBuildDetailDto(build, compatibility);
}

function createShareCode() {
  return crypto.randomBytes(6).toString("hex");
}

export async function getMyPcBuilds(userId, query) {
  const page = normalizePage(query?.page);
  const limit = normalizeLimit(query?.limit);
  const [items, total] = await findMyPcBuilds(userId, page, limit);

  return {
    items: items.map(toPcBuildSummaryDto),
    meta: paginate(total, page, limit),
  };
}

export async function createPcBuild(userId, body) {
  const build = await createPcBuildRecord({
    userId: Number(userId),
    name: body.name.trim(),
    description: body.description?.trim() || null,
    purpose: body.purpose,
    budget: body.budget === undefined || body.budget === null ? null : Number(body.budget),
    totalPrice: 0,
    shareCode: null,
    viewCount: 0,
  });

  return toBuildDetail(build);
}

export async function getPcBuild(buildId) {
  const build = await getBuildOrThrow(buildId);
  await incrementPcBuildViewCount(build.id);
  const refreshed = await getBuildOrThrow(buildId);
  return toBuildDetail(refreshed);
}

export async function updatePcBuild(userId, buildId, body) {
  await getOwnedBuildOrThrow(userId, buildId);
  const updated = await updatePcBuildRecord(buildId, {
    ...(body.name !== undefined ? { name: body.name.trim() } : {}),
    ...(body.description !== undefined ? { description: body.description?.trim() || null } : {}),
    ...(body.purpose !== undefined ? { purpose: body.purpose } : {}),
    ...(body.budget !== undefined ? { budget: body.budget === null ? null : Number(body.budget) } : {}),
  });

  return toBuildDetail(updated);
}

export async function deletePcBuild(userId, buildId) {
  await getOwnedBuildOrThrow(userId, buildId);
  await deletePcBuildRecord(buildId);
  return { message: "PC build deleted" };
}

export async function addPcBuildPart(userId, buildId, body) {
  const build = await getOwnedBuildOrThrow(userId, buildId);
  const product = await findProductForPcBuild(body.productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const selectedPrice = body.sellerId
    ? await findPriceEntryForPcBuild(body.productId, body.sellerId)
    : await findBestPriceEntryForPcBuild(body.productId);
  if (!selectedPrice) {
    throw badRequest("No available seller price for this product");
  }

  const quantity = body.quantity === undefined ? 1 : Number(body.quantity);
  const payload = {
    buildId: build.id,
    productId: product.id,
    sellerId: selectedPrice.sellerId,
    partType: body.partType,
    quantity,
    unitPrice: selectedPrice.price,
    totalPrice: selectedPrice.price * quantity,
  };

  const existing = await findPcBuildPartByType(build.id, body.partType);
  if (existing) {
    await updatePcBuildPartRecord(existing.id, payload);
  } else {
    await createPcBuildPartRecord(payload);
  }

  const synced = await syncPcBuildTotalPrice(build.id);
  return toBuildDetail(synced);
}

export async function removePcBuildPart(userId, buildId, partId) {
  await getOwnedBuildOrThrow(userId, buildId);
  const part = await findPcBuildPartById(buildId, partId);
  if (!part) {
    throw notFound("PC build part not found");
  }

  await deletePcBuildPartRecord(part.id);
  const synced = await syncPcBuildTotalPrice(buildId);
  return toBuildDetail(synced);
}

export async function getPcBuildCompatibility(buildId) {
  const build = await getBuildOrThrow(buildId);
  return evaluateCompatibility(build);
}

export async function sharePcBuild(userId, buildId) {
  const build = await getOwnedBuildOrThrow(userId, buildId);
  if (build.shareCode) {
    return {
      shareCode: build.shareCode,
      shareUrl: `/api/v1/pc-builds/shared/${build.shareCode}`,
    };
  }

  for (let attempt = 0; attempt < 5; attempt += 1) {
    const shareCode = createShareCode();
    const duplicated = await findPcBuildByShareCode(shareCode);
    if (duplicated) {
      continue;
    }

    const updated = await updatePcBuildRecord(build.id, { shareCode });
    return {
      shareCode: updated.shareCode,
      shareUrl: `/api/v1/pc-builds/shared/${updated.shareCode}`,
    };
  }

  throw conflict("Could not generate unique share code");
}

export async function getSharedPcBuild(shareCode) {
  const build = await findPcBuildByShareCode(String(shareCode));
  if (!build) {
    throw notFound("Shared PC build not found");
  }

  await incrementPcBuildViewCount(build.id);
  const refreshed = await getBuildOrThrow(build.id);
  return toBuildDetail(refreshed);
}

export async function getPopularPcBuilds(query) {
  const page = normalizePage(query?.page);
  const limit = normalizeLimit(query?.limit);
  const [items, total] = await findPopularPcBuilds(page, limit);

  return {
    items: items.map(toPcBuildSummaryDto),
    meta: paginate(total, page, limit),
  };
}

export async function getCompatibilityRules() {
  const items = await listCompatibilityRules();
  return items.map(toCompatibilityRuleDto);
}

export async function createCompatibilityRule(body) {
  const created = await createCompatibilityRuleRecord({
    partType: body.partType,
    targetPartType: body.targetPartType ?? null,
    title: body.title.trim(),
    description: body.description.trim(),
    severity: body.severity ?? "MEDIUM",
    enabled: body.enabled ?? true,
    metadata: body.metadata ?? null,
  });

  return toCompatibilityRuleDto(created);
}

export async function updateCompatibilityRule(ruleId, body) {
  const rule = await findCompatibilityRuleById(ruleId);
  if (!rule) {
    throw notFound("Compatibility rule not found");
  }

  const updated = await updateCompatibilityRuleRecord(rule.id, {
    ...(body.partType !== undefined ? { partType: body.partType } : {}),
    ...(body.targetPartType !== undefined ? { targetPartType: body.targetPartType } : {}),
    ...(body.title !== undefined ? { title: body.title.trim() } : {}),
    ...(body.description !== undefined ? { description: body.description.trim() } : {}),
    ...(body.severity !== undefined ? { severity: body.severity } : {}),
    ...(body.enabled !== undefined ? { enabled: body.enabled } : {}),
    ...(body.metadata !== undefined ? { metadata: body.metadata } : {}),
  });

  return toCompatibilityRuleDto(updated);
}

export async function deleteCompatibilityRule(ruleId) {
  const rule = await findCompatibilityRuleById(ruleId);
  if (!rule) {
    throw notFound("Compatibility rule not found");
  }

  await deleteCompatibilityRuleRecord(rule.id);
  return { message: "Compatibility rule deleted" };
}

export function validatePartType(value) {
  return REQUIRED_PARTS.includes(value) || ["HDD", "COOLER", "MONITOR"].includes(value);
}

export function validateCompatibilitySeverity(value) {
  return SEVERITIES.includes(value);
}
