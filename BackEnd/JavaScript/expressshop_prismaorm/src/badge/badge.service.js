import {
  conflict,
  badRequest,
  notFound,
} from "../utils/http-error.js";
import {
  countUserBadges,
  createBadge,
  createUserBadge,
  deactivateBadgeAndDeleteUserBadges,
  deleteUserBadge,
  findActiveBadges,
  findBadgeById,
  findBadgeByName,
  findUserBadge,
  findUserBadges,
  findUserById,
  updateBadge,
  updateBadgeHolderCount,
} from "./badge.repository.js";

const BADGE_TYPES = new Set(["AUTO", "MANUAL"]);
const BADGE_RARITIES = new Set(["COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY"]);

function normalizeType(type, fallback) {
  if (type == null) return fallback;
  return String(type).trim().toUpperCase();
}

function normalizeRarity(rarity, fallback = "COMMON") {
  if (rarity == null) return fallback;
  return String(rarity).trim().toUpperCase();
}

function normalizeCondition(condition) {
  if (condition == null) return null;
  return typeof condition === "object" && !Array.isArray(condition) ? condition : null;
}

function ensureValidBadgeState(type, condition) {
  if (!BADGE_TYPES.has(type)) {
    throw badRequest("type must be one of AUTO, MANUAL");
  }

  if (type === "AUTO" && !condition) {
    throw badRequest("AUTO badge requires condition");
  }
}

function ensureValidRarity(rarity) {
  if (!BADGE_RARITIES.has(rarity)) {
    throw badRequest("rarity must be one of COMMON, UNCOMMON, RARE, EPIC, LEGENDARY");
  }
}

function toBadgeDto(item, holderCount = item.holderCount) {
  return {
    id: item.id,
    name: item.name,
    description: item.description,
    iconUrl: item.iconUrl,
    type: item.type,
    condition: item.condition,
    rarity: item.rarity,
    holderCount,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

function toUserBadgeDto(item, holderCount = item.badge?.holderCount ?? 0) {
  return {
    id: item.id,
    userId: item.userId,
    badgeId: item.badgeId,
    grantedByAdminId: item.grantedByAdminId,
    reason: item.reason,
    grantedAt: item.grantedAt,
    badge: item.badge
      ? {
          id: item.badge.id,
          name: item.badge.name,
          description: item.badge.description,
          iconUrl: item.badge.iconUrl,
          type: item.badge.type,
          condition: item.badge.condition,
          rarity: item.badge.rarity,
          holderCount,
        }
      : null,
  };
}

async function ensureBadgeExists(badgeId) {
  const badge = await findBadgeById(badgeId);
  if (!badge) {
    throw notFound("Badge not found");
  }
  return badge;
}

async function ensureActiveBadge(badgeId) {
  const badge = await ensureBadgeExists(badgeId);
  if (!badge.isActive) {
    throw badRequest("Badge is inactive");
  }
  return badge;
}

async function ensureUserExists(userId) {
  const user = await findUserById(userId);
  if (!user || user.deletedAt) {
    throw notFound("User not found");
  }
  return user;
}

async function refreshHolderCount(badgeId) {
  const holderCount = await countUserBadges(badgeId);
  await updateBadgeHolderCount(badgeId, holderCount);
  return holderCount;
}

export async function getBadges() {
  const items = await findActiveBadges();
  const data = await Promise.all(
    items.map(async (item) => {
      const holderCount = await countUserBadges(item.id);
      if (item.holderCount !== holderCount) {
        await updateBadgeHolderCount(item.id, holderCount);
      }
      return toBadgeDto(item, holderCount);
    }),
  );

  return data;
}

export async function getMyBadges(userId) {
  await ensureUserExists(userId);
  const items = await findUserBadges(userId);
  return Promise.all(
    items.map(async (item) => {
      const holderCount = await countUserBadges(item.badgeId);
      return toUserBadgeDto(item, holderCount);
    }),
  );
}

export async function getUserBadges(userId) {
  await ensureUserExists(userId);
  const items = await findUserBadges(userId);
  return Promise.all(
    items.map(async (item) => {
      const holderCount = await countUserBadges(item.badgeId);
      return toUserBadgeDto(item, holderCount);
    }),
  );
}

export async function createBadgeItem(payload) {
  const name = String(payload?.name ?? "").trim();
  const description = String(payload?.description ?? "").trim();
  const iconUrl = String(payload?.iconUrl ?? "").trim();
  const type = normalizeType(payload?.type, "AUTO");
  const condition = normalizeCondition(payload?.condition);
  const rarity = normalizeRarity(payload?.rarity, "COMMON");

  if (!name || !description || !iconUrl) {
    throw badRequest("name, description, iconUrl are required");
  }

  ensureValidBadgeState(type, condition);
  ensureValidRarity(rarity);

  const existing = await findBadgeByName(name);
  if (existing) {
    throw conflict("Badge name already exists");
  }

  const item = await createBadge({
    name,
    description,
    iconUrl,
    type,
    condition,
    rarity,
  });

  return toBadgeDto(item, 0);
}

export async function updateBadgeItem(badgeId, payload) {
  const badge = await ensureBadgeExists(badgeId);
  const data = {};

  if (payload?.name !== undefined) {
    const name = String(payload.name).trim();
    if (!name) {
      throw badRequest("name must not be empty");
    }
    const existing = await findBadgeByName(name);
    if (existing && existing.id !== badge.id) {
      throw conflict("Badge name already exists");
    }
    data.name = name;
  }

  if (payload?.description !== undefined) {
    const description = String(payload.description).trim();
    if (!description) {
      throw badRequest("description must not be empty");
    }
    data.description = description;
  }

  if (payload?.iconUrl !== undefined) {
    const iconUrl = String(payload.iconUrl).trim();
    if (!iconUrl) {
      throw badRequest("iconUrl must not be empty");
    }
    data.iconUrl = iconUrl;
  }

  const type = normalizeType(payload?.type, badge.type);
  const rarity = normalizeRarity(payload?.rarity, badge.rarity);
  const condition = payload?.condition !== undefined ? normalizeCondition(payload.condition) : badge.condition;

  ensureValidBadgeState(type, condition);
  ensureValidRarity(rarity);

  data.type = type;
  data.rarity = rarity;
  if (payload?.condition !== undefined) {
    data.condition = condition;
  }

  const item = await updateBadge(badgeId, data);
  const holderCount = await refreshHolderCount(item.id);
  return toBadgeDto(item, holderCount);
}

export async function deleteBadgeItem(badgeId) {
  await ensureBadgeExists(badgeId);
  await deactivateBadgeAndDeleteUserBadges(badgeId);
  return { message: "Badge deleted" };
}

export async function grantBadgeItem(adminId, badgeId, payload) {
  const badge = await ensureActiveBadge(badgeId);
  const userId = Number(payload?.userId);

  if (!userId) {
    throw badRequest("userId is required");
  }

  await ensureUserExists(userId);

  const existing = await findUserBadge(badgeId, userId);
  if (existing) {
    throw conflict("Badge already granted");
  }

  const item = await createUserBadge({
    userId,
    badgeId: badge.id,
    grantedByAdminId: Number(adminId),
    reason: payload?.reason ? String(payload.reason).trim() : null,
    grantedAt: new Date(),
  });

  const holderCount = await refreshHolderCount(badge.id);
  return toUserBadgeDto(item, holderCount);
}

export async function revokeBadgeItem(badgeId, userId) {
  const item = await findUserBadge(badgeId, userId);
  if (!item) {
    throw notFound("Granted badge not found");
  }

  await deleteUserBadge(badgeId, userId);
  await refreshHolderCount(badgeId);
  return { message: "Badge revoked" };
}
