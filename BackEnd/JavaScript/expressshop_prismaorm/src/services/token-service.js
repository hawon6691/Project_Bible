import jwt from "jsonwebtoken";

import { unauthorized } from "../utils/http-error.js";

const ACCESS_TOKEN_EXPIRES_IN = "1h";
const REFRESH_TOKEN_EXPIRES_IN = "7d";

function getSecret(name, fallback) {
  return process.env[name] ?? fallback;
}

export function signAccessToken(user) {
  return jwt.sign(
    {
      sub: user.id,
      email: user.email,
      role: user.role,
    },
    getSecret("JWT_ACCESS_SECRET", "pbshop-access-secret"),
    { expiresIn: ACCESS_TOKEN_EXPIRES_IN },
  );
}

export function signRefreshToken(user) {
  return jwt.sign(
    {
      sub: user.id,
      email: user.email,
      role: user.role,
      type: "refresh",
    },
    getSecret("JWT_REFRESH_SECRET", "pbshop-refresh-secret"),
    { expiresIn: REFRESH_TOKEN_EXPIRES_IN },
  );
}

export function verifyAccessToken(token) {
  try {
    return jwt.verify(token, getSecret("JWT_ACCESS_SECRET", "pbshop-access-secret"));
  } catch {
    throw unauthorized("Invalid access token");
  }
}

export function verifyRefreshToken(token) {
  try {
    return jwt.verify(token, getSecret("JWT_REFRESH_SECRET", "pbshop-refresh-secret"));
  } catch {
    throw unauthorized("Invalid refresh token");
  }
}

export function tokenResponse(user) {
  return {
    accessToken: signAccessToken(user),
    refreshToken: signRefreshToken(user),
    expiresIn: 3600,
  };
}
