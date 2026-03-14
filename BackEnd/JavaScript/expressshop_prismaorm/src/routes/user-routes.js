import bcrypt from "bcryptjs";
import { Router } from "express";

import { prisma } from "../prisma.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";
import { badRequest, notFound } from "../utils/http-error.js";
import { success } from "../utils/response.js";

function userResponse(user) {
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

export function createUserRoutes(apiPrefix) {
  const router = Router();

  router.get(
    `${apiPrefix}/users/me`,
    requireAuth,
    asyncHandler(async (req, res) => {
      res.status(200).json(success(userResponse(req.user)));
    }),
  );

  router.patch(
    `${apiPrefix}/users/me`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const { name, phone, password } = req.body ?? {};
      const data = {};

      if (name) data.name = name;
      if (phone) data.phone = phone;
      if (password) data.password = await bcrypt.hash(password, 10);

      if (Object.keys(data).length === 0) {
        throw badRequest("At least one field is required");
      }

      const user = await prisma.user.update({
        where: { id: req.user.id },
        data,
      });

      res.status(200).json(success(userResponse(user)));
    }),
  );

  router.get(
    `${apiPrefix}/users`,
    requireAuth,
    requireRole("ADMIN"),
    asyncHandler(async (req, res) => {
      const page = Number(req.query.page ?? 1);
      const limit = Math.min(Number(req.query.limit ?? 20), 100);
      const search = String(req.query.search ?? "").trim();
      const where = search
        ? {
            OR: [
              { email: { contains: search, mode: "insensitive" } },
              { name: { contains: search, mode: "insensitive" } },
              { nickname: { contains: search, mode: "insensitive" } },
            ],
          }
        : {};

      const [items, total] = await Promise.all([
        prisma.user.findMany({
          where,
          skip: (page - 1) * limit,
          take: limit,
          orderBy: { id: "asc" },
        }),
        prisma.user.count({ where }),
      ]);

      res.status(200).json(
        success(
          items.map(userResponse),
          { page, limit, total },
        ),
      );
    }),
  );

  router.get(
    `${apiPrefix}/users/:id/profile`,
    asyncHandler(async (req, res) => {
      const user = await prisma.user.findUnique({
        where: { id: Number(req.params.id) },
        select: {
          id: true,
          name: true,
          nickname: true,
          bio: true,
          profileImageUrl: true,
          role: true,
          createdAt: true,
        },
      });

      if (!user) {
        throw notFound("User profile not found");
      }

      res.status(200).json(success(user));
    }),
  );

  return router;
}
