import bcrypt from "bcryptjs";
import { Router } from "express";

import { prisma } from "../prisma.js";
import { requireAuth } from "../middleware/auth.js";
import {
  signRefreshToken,
  tokenResponse,
  verifyRefreshToken,
} from "../services/token-service.js";
import { asyncHandler } from "../utils/async-handler.js";
import { badRequest, unauthorized } from "../utils/http-error.js";
import { success } from "../utils/response.js";

function sanitizeUser(user) {
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

export function createAuthRoutes(apiPrefix) {
  const router = Router();

  router.post(
    `${apiPrefix}/auth/signup`,
    asyncHandler(async (req, res) => {
      const { email, password, name, phone } = req.body ?? {};
      if (!email || !password || !name || !phone) {
        throw badRequest("email, password, name, phone are required");
      }

      const existing = await prisma.user.findUnique({ where: { email } });
      if (existing) {
        throw badRequest("Email already exists");
      }

      const hashedPassword = await bcrypt.hash(password, 10);
      const nicknameBase = email.split("@")[0].slice(0, 24) || "user";
      const nickname = `${nicknameBase}${Date.now().toString().slice(-4)}`;

      const user = await prisma.user.create({
        data: {
          email,
          password: hashedPassword,
          name,
          phone,
          nickname,
          emailVerified: false,
        },
      });

      res.status(201).json(
        success({
          id: user.id,
          email: user.email,
          name: user.name,
          message: "Signup completed. Verify your email to activate login.",
        }),
      );
    }),
  );

  router.post(
    `${apiPrefix}/auth/login`,
    asyncHandler(async (req, res) => {
      const { email, password } = req.body ?? {};
      if (!email || !password) {
        throw badRequest("email and password are required");
      }

      const user = await prisma.user.findUnique({ where: { email } });
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
      await prisma.user.update({
        where: { id: user.id },
        data: { refreshToken: tokens.refreshToken },
      });

      res.status(200).json(success(tokens));
    }),
  );

  router.post(
    `${apiPrefix}/auth/logout`,
    requireAuth,
    asyncHandler(async (req, res) => {
      await prisma.user.update({
        where: { id: req.user.id },
        data: { refreshToken: null },
      });

      res.status(200).json(success({ message: "Logged out" }));
    }),
  );

  router.post(
    `${apiPrefix}/auth/refresh`,
    asyncHandler(async (req, res) => {
      const { refreshToken } = req.body ?? {};
      if (!refreshToken) {
        throw badRequest("refreshToken is required");
      }

      const payload = verifyRefreshToken(refreshToken);
      const user = await prisma.user.findUnique({
        where: { id: Number(payload.sub) },
      });

      if (!user || user.refreshToken !== refreshToken) {
        throw unauthorized("Refresh token is not valid");
      }

      const nextRefreshToken = signRefreshToken(user);
      const tokens = {
        ...tokenResponse(user),
        refreshToken: nextRefreshToken,
      };

      await prisma.user.update({
        where: { id: user.id },
        data: { refreshToken: nextRefreshToken },
      });

      res.status(200).json(success(tokens));
    }),
  );

  router.get(
    `${apiPrefix}/auth/me`,
    requireAuth,
    asyncHandler(async (req, res) => {
      res.status(200).json(success(sanitizeUser(req.user)));
    }),
  );

  return router;
}
