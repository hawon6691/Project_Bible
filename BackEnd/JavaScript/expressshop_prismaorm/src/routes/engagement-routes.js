import { Router } from "express";

import { prisma } from "../prisma.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";
import { badRequest, notFound } from "../utils/http-error.js";
import { success } from "../utils/response.js";

export function createEngagementRoutes(apiPrefix) {
  const router = Router();

  router.get(
    `${apiPrefix}/products/:productId/reviews`,
    asyncHandler(async (req, res) => {
      const productId = Number(req.params.productId);
      const items = await prisma.review.findMany({
        where: { productId, deletedAt: null },
        include: {
          user: {
            select: {
              id: true,
              name: true,
              nickname: true,
            },
          },
        },
        orderBy: [{ isBest: "desc" }, { id: "desc" }],
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.post(
    `${apiPrefix}/products/:productId/reviews`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const productId = Number(req.params.productId);
      const { orderId, rating, content } = req.body ?? {};
      if (!orderId || !rating || !content) {
        throw badRequest("orderId, rating, content are required");
      }

      const created = await prisma.review.create({
        data: {
          userId: req.user.id,
          productId,
          orderId: Number(orderId),
          rating: Number(rating),
          content,
        },
      });

      res.status(201).json(success(created));
    }),
  );

  router.get(
    `${apiPrefix}/wishlist`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const page = Number(req.query.page ?? 1);
      const limit = Math.min(Number(req.query.limit ?? 20), 100);
      const [items, total] = await Promise.all([
        prisma.wishlist.findMany({
          where: { userId: req.user.id },
          include: {
            product: {
              select: {
                id: true,
                name: true,
                thumbnailUrl: true,
                lowestPrice: true,
                averageRating: true,
              },
            },
          },
          skip: (page - 1) * limit,
          take: limit,
          orderBy: { id: "desc" },
        }),
        prisma.wishlist.count({ where: { userId: req.user.id } }),
      ]);

      res.status(200).json(success(items, { page, limit, total }));
    }),
  );

  router.post(
    `${apiPrefix}/wishlist/:productId`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const productId = Number(req.params.productId);
      const existing = await prisma.wishlist.findFirst({
        where: { userId: req.user.id, productId },
      });

      if (existing) {
        await prisma.wishlist.delete({ where: { id: existing.id } });
        res.status(200).json(success({ wishlisted: false }));
        return;
      }

      await prisma.wishlist.create({
        data: {
          userId: req.user.id,
          productId,
        },
      });

      res.status(201).json(success({ wishlisted: true }));
    }),
  );

  router.delete(
    `${apiPrefix}/wishlist/:productId`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const productId = Number(req.params.productId);
      await prisma.wishlist.deleteMany({
        where: { userId: req.user.id, productId },
      });

      res.status(200).json(success({ message: "Wishlist item deleted" }));
    }),
  );

  router.get(
    `${apiPrefix}/points/balance`,
    requireAuth,
    asyncHandler(async (req, res) => {
      res.status(200).json(success({ balance: req.user.point }));
    }),
  );

  router.get(
    `${apiPrefix}/points/transactions`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const items = await prisma.pointTransaction.findMany({
        where: { userId: req.user.id },
        orderBy: { id: "desc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.post(
    `${apiPrefix}/admin/points/grant`,
    requireAuth,
    requireRole("ADMIN"),
    asyncHandler(async (req, res) => {
      const { userId, amount, description } = req.body ?? {};
      if (!userId || !amount || !description) {
        throw badRequest("userId, amount, description are required");
      }

      const user = await prisma.user.findUnique({
        where: { id: Number(userId) },
      });

      if (!user) {
        throw notFound("User not found");
      }

      const balance = user.point + Number(amount);

      await prisma.user.update({
        where: { id: user.id },
        data: { point: balance },
      });

      const transaction = await prisma.pointTransaction.create({
        data: {
          userId: user.id,
          type: "ADMIN_GRANT",
          amount: Number(amount),
          balance,
          description,
          referenceType: "ADMIN",
          referenceId: req.user.id,
        },
      });

      res.status(201).json(success(transaction));
    }),
  );

  return router;
}
