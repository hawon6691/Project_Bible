import bcrypt from "bcryptjs";

import {
  findUserById,
  findUserByNickname,
  findUserProfile,
  findUsers,
  softDeleteUser,
  updateUser,
} from "./user.repository.js";
import { badRequest, conflict, notFound } from "../utils/http-error.js";

const USER_STATUSES = new Set(["ACTIVE", "INACTIVE", "BLOCKED"]);
const USER_ROLES = new Set(["USER", "SELLER", "ADMIN"]);

export function userResponse(user) {
  return {
    id: user.id,
    email: user.email,
    name: user.name,
    phone: user.phone,
    role: user.role,
    status: user.status,
    nickname: user.nickname,
    bio: user.bio,
    profileImageUrl: user.profileImageUrl,
    point: user.point,
    preferredLocale: user.preferredLocale,
    preferredCurrency: user.preferredCurrency,
    createdAt: user.createdAt,
    updatedAt: user.updatedAt,
  };
}

export async function getMe(user) {
  return userResponse(user);
}

export async function updateMe(userId, payload) {
  const { name, phone, password } = payload ?? {};
  const data = {};

  if (name) data.name = name;
  if (phone) data.phone = phone;
  if (password) data.password = await bcrypt.hash(password, 10);

  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  const user = await updateUser(userId, data);
  return userResponse(user);
}

export async function deleteMe(userId) {
  const user = await findUserById(userId);
  if (!user || user.deletedAt) {
    throw notFound("User not found");
  }

  await softDeleteUser(userId);
  return { message: "User deleted" };
}

export async function updateMyProfile(userId, payload) {
  const { nickname, bio } = payload ?? {};
  if (!nickname && bio === undefined) {
    throw badRequest("nickname or bio is required");
  }

  const user = await findUserById(userId);
  if (!user || user.deletedAt) {
    throw notFound("User not found");
  }

  if (nickname && nickname !== user.nickname) {
    const existing = await findUserByNickname(nickname);
    if (existing && existing.id !== Number(userId)) {
      throw conflict("Nickname already exists");
    }
  }

  const updated = await updateUser(userId, {
    ...(nickname ? { nickname } : {}),
    ...(bio !== undefined ? { bio } : {}),
  });
  return updated;
}

export async function updateUserStatus(userId, payload) {
  const status = String(payload?.status ?? "").trim().toUpperCase();
  if (!USER_STATUSES.has(status)) {
    throw badRequest("status must be ACTIVE, INACTIVE, or BLOCKED");
  }

  const user = await findUserById(userId);
  if (!user || user.deletedAt) {
    throw notFound("User not found");
  }

  const updated = await updateUser(userId, { status });
  return userResponse(updated);
}

export async function getUsers(query) {
  const page = Number(query.page ?? 1);
  const limit = Math.min(Number(query.limit ?? 20), 100);
  const search = String(query.search ?? "").trim();
  const status = query.status ? String(query.status).trim().toUpperCase() : undefined;
  const role = query.role ? String(query.role).trim().toUpperCase() : undefined;
  const where = {
    deletedAt: null,
    ...(status && USER_STATUSES.has(status) ? { status } : {}),
    ...(role && USER_ROLES.has(role) ? { role } : {}),
    ...(search
      ? {
          OR: [
            { email: { contains: search, mode: "insensitive" } },
            { name: { contains: search, mode: "insensitive" } },
            { nickname: { contains: search, mode: "insensitive" } },
          ],
        }
      : {}),
  };

  const [items, total] = await findUsers(where, page, limit);
  return {
    items: items.map(userResponse),
    meta: { page, limit, total },
  };
}

export async function getProfile(userId) {
  const user = await findUserProfile(userId);
  if (!user || user.deletedAt) {
    throw notFound("User profile not found");
  }
  return user;
}
