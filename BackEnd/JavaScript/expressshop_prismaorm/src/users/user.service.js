import bcrypt from "bcryptjs";

import { findUserProfile, findUsers, updateUser } from "./user.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

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

export async function getUsers(query) {
  const page = Number(query.page ?? 1);
  const limit = Math.min(Number(query.limit ?? 20), 100);
  const search = String(query.search ?? "").trim();
  const where = search
    ? {
        OR: [
          { email: { contains: search, mode: "insensitive" } },
          { name: { contains: search, mode: "insensitive" } },
          { nickname: { contains: search, mode: "insensitive" } },
        ],
      }
    : {};

  const [items, total] = await findUsers(where, page, limit);
  return {
    items: items.map(userResponse),
    meta: { page, limit, total },
  };
}

export async function getProfile(userId) {
  const user = await findUserProfile(userId);
  if (!user) {
    throw notFound("User profile not found");
  }
  return user;
}
