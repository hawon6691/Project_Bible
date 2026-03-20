import jwt from "jsonwebtoken";

import { jwtConfig } from "../config/jwt.js";
import { unauthorized } from "../utils/http-error.js";

export function signAccessToken(user) {
  return jwt.sign(
    {
      sub: user.id,
      email: user.email,
      role: user.role,
    },
    jwtConfig.accessSecret,
    { expiresIn: jwtConfig.accessExpiresIn },
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
    jwtConfig.refreshSecret,
    { expiresIn: jwtConfig.refreshExpiresIn },
  );
}

export function verifyAccessToken(token) {
  try {
    return jwt.verify(token, jwtConfig.accessSecret);
  } catch {
    throw unauthorized("Invalid access token");
  }
}

export function verifyRefreshToken(token) {
  try {
    return jwt.verify(token, jwtConfig.refreshSecret);
  } catch {
    throw unauthorized("Invalid refresh token");
  }
}

export function signPasswordResetToken(user) {
  return jwt.sign(
    {
      sub: user.id,
      email: user.email,
      type: "password-reset",
    },
    jwtConfig.refreshSecret,
    { expiresIn: "5m" },
  );
}

export function verifyPasswordResetToken(token) {
  try {
    const payload = jwt.verify(token, jwtConfig.refreshSecret);
    if (payload?.type !== "password-reset") {
      throw new Error("invalid token type");
    }
    return payload;
  } catch {
    throw unauthorized("Invalid reset token");
  }
}

export function tokenResponse(user) {
  return {
    accessToken: signAccessToken(user),
    refreshToken: signRefreshToken(user),
    expiresIn: 3600,
  };
}
