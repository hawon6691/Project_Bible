import bcrypt from "bcryptjs";

import {
  createEmailVerification,
  createUser,
  findActiveEmailVerification,
  findUserByEmail,
  findUserById,
  incrementEmailVerificationAttempts,
  markEmailVerificationsUsed,
  updateUserAccount,
  updateUserRefreshToken,
} from "./auth.repository.js";
import {
  signPasswordResetToken,
  signRefreshToken,
  tokenResponse,
  verifyPasswordResetToken,
  verifyRefreshToken,
} from "./token.service.js";
import {
  badRequest,
  conflict,
  gone,
  notFound,
  tooManyRequests,
  unauthorized,
} from "../utils/http-error.js";

const VERIFICATION_EXPIRY_MS = 10 * 60 * 1000;
const RESEND_COOLDOWN_MS = 60 * 1000;
const MAX_VERIFICATION_ATTEMPTS = 5;

function generateVerificationCode() {
  return String(Math.floor(100000 + Math.random() * 900000));
}

async function issueVerification(user, type) {
  const active = await findActiveEmailVerification(user.id, type);
  if (active && Date.now() - new Date(active.createdAt).getTime() < RESEND_COOLDOWN_MS) {
    throw tooManyRequests("Please wait a minute before requesting another code");
  }

  await markEmailVerificationsUsed(user.id, type);

  return createEmailVerification({
    userId: user.id,
    type,
    code: generateVerificationCode(),
    expiresAt: new Date(Date.now() + VERIFICATION_EXPIRY_MS),
  });
}

async function getValidVerification(user, type, code) {
  const active = await findActiveEmailVerification(user.id, type);
  if (!active) {
    throw badRequest("Verification code is not valid");
  }

  if (active.attemptCount >= MAX_VERIFICATION_ATTEMPTS) {
    throw tooManyRequests("Verification attempt limit exceeded");
  }

  if (new Date(active.expiresAt).getTime() < Date.now()) {
    throw gone("Verification code has expired");
  }

  if (active.code !== code) {
    await incrementEmailVerificationAttempts(active.id);
    throw badRequest("Verification code is not valid");
  }

  return active;
}

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
    throw conflict("Email already exists");
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

  await issueVerification(user, "SIGNUP");

  return {
    id: user.id,
    email: user.email,
    name: user.name,
    message: "Signup completed. Check your email verification code.",
  };
}

export async function verifyEmail(payload) {
  const { email, code } = payload ?? {};
  if (!email || !code) {
    throw badRequest("email and code are required");
  }

  const user = await findUserByEmail(email);
  if (!user) {
    throw notFound("User not found");
  }

  if (user.emailVerified) {
    throw conflict("Email is already verified");
  }

  await getValidVerification(user, "SIGNUP", code);
  await markEmailVerificationsUsed(user.id, "SIGNUP");
  await updateUserAccount(user.id, {
    emailVerified: true,
    emailVerifiedAt: new Date(),
  });

  return {
    message: "Email verification completed",
    verified: true,
  };
}

export async function resendVerification(payload) {
  const { email } = payload ?? {};
  if (!email) {
    throw badRequest("email is required");
  }

  const user = await findUserByEmail(email);
  if (!user) {
    throw notFound("User not found");
  }

  if (user.emailVerified) {
    throw conflict("Email is already verified");
  }

  await issueVerification(user, "SIGNUP");
  return { message: "Verification code resent" };
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

export async function requestPasswordReset(payload) {
  const { email, phone } = payload ?? {};
  if (!email || !phone) {
    throw badRequest("email and phone are required");
  }

  const user = await findUserByEmail(email);
  if (!user) {
    throw notFound("User not found");
  }

  if (user.phone !== phone) {
    throw badRequest("Phone number does not match");
  }

  await issueVerification(user, "PASSWORD_RESET");
  return { message: "Password reset verification code sent" };
}

export async function verifyPasswordReset(payload) {
  const { email, code } = payload ?? {};
  if (!email || !code) {
    throw badRequest("email and code are required");
  }

  const user = await findUserByEmail(email);
  if (!user) {
    throw notFound("User not found");
  }

  await getValidVerification(user, "PASSWORD_RESET", code);
  await markEmailVerificationsUsed(user.id, "PASSWORD_RESET");

  return {
    message: "Password reset verified",
    resetToken: signPasswordResetToken(user),
  };
}

export async function confirmPasswordReset(payload) {
  const { resetToken, newPassword } = payload ?? {};
  if (!resetToken || !newPassword) {
    throw badRequest("resetToken and newPassword are required");
  }

  const jwtPayload = verifyPasswordResetToken(resetToken);
  const user = await findUserById(jwtPayload.sub);
  if (!user) {
    throw notFound("User not found");
  }

  const isSamePassword = await bcrypt.compare(newPassword, user.password);
  if (isSamePassword) {
    throw badRequest("New password must be different");
  }

  const hashedPassword = await bcrypt.hash(newPassword, 10);
  await updateUserAccount(user.id, {
    password: hashedPassword,
    refreshToken: null,
  });

  return { message: "Password changed successfully" };
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
