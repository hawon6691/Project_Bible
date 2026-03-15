import bcrypt from "bcryptjs";

import {
  createUser,
  findUserByEmail,
  findUserById,
  updateUserRefreshToken,
} from "./auth.repository.js";
import {
  signRefreshToken,
  tokenResponse,
  verifyRefreshToken,
} from "./token.service.js";
import { badRequest, unauthorized } from "../utils/http-error.js";

export function sanitizeAuthUser(user) {
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
  };
}

export async function signup(payload) {
  const { email, password, name, phone } = payload ?? {};
  if (!email || !password || !name || !phone) {
    throw badRequest("email, password, name, phone are required");
  }

  const existing = await findUserByEmail(email);
  if (existing) {
    throw badRequest("Email already exists");
  }

  const hashedPassword = await bcrypt.hash(password, 10);
  const nicknameBase = email.split("@")[0].slice(0, 24) || "user";
  const nickname = `${nicknameBase}${Date.now().toString().slice(-4)}`;

  const user = await createUser({
    email,
    password: hashedPassword,
    name,
    phone,
    nickname,
    emailVerified: false,
  });

  return {
    id: user.id,
    email: user.email,
    name: user.name,
    message: "Signup completed. Verify your email to activate login.",
  };
}

export async function login(payload) {
  const { email, password } = payload ?? {};
  if (!email || !password) {
    throw badRequest("email and password are required");
  }

  const user = await findUserByEmail(email);
  if (!user) {
    throw unauthorized("Invalid email or password");
  }

  const matched = await bcrypt.compare(password, user.password);
  if (!matched) {
    throw unauthorized("Invalid email or password");
  }

  if (user.status !== "ACTIVE") {
    throw unauthorized("User is not active");
  }

  if (!user.emailVerified) {
    throw unauthorized("Email verification is required");
  }

  const tokens = tokenResponse(user);
  await updateUserRefreshToken(user.id, tokens.refreshToken);
  return tokens;
}

export async function logout(userId) {
  await updateUserRefreshToken(userId, null);
  return { message: "Logged out" };
}

export async function refreshTokens(payload) {
  const { refreshToken } = payload ?? {};
  if (!refreshToken) {
    throw badRequest("refreshToken is required");
  }

  const jwtPayload = verifyRefreshToken(refreshToken);
  const user = await findUserById(jwtPayload.sub);

  if (!user || user.refreshToken !== refreshToken) {
    throw unauthorized("Refresh token is not valid");
  }

  const nextRefreshToken = signRefreshToken(user);
  const tokens = {
    ...tokenResponse(user),
    refreshToken: nextRefreshToken,
  };

  await updateUserRefreshToken(user.id, nextRefreshToken);
  return tokens;
}
