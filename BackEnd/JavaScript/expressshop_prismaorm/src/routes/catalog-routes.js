import { Router } from "express";

import { prisma } from "../prisma.js";
import { asyncHandler } from "../utils/async-handler.js";
import { notFound } from "../utils/http-error.js";
import { success } from "../utils/response.js";

export function createCatalogRoutes(apiPrefix) {
  const router = Router();

  router.get(
    `${apiPrefix}/categories`,
    asyncHandler(async (_req, res) => {
      const items = await prisma.category.findMany({
        select: {
          id: true,
          name: true,
          parentId: true,
          sortOrder: true,
          createdAt: true,
          updatedAt: true,
        },
        orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.get(
    `${apiPrefix}/products`,
    asyncHandler(async (req, res) => {
      const page = Number(req.query.page ?? 1);
      const limit = Math.min(Number(req.query.limit ?? 20), 100);
      const categoryId = req.query.categoryId ? Number(req.query.categoryId) : undefined;
      const search = String(req.query.search ?? "").trim();
      const where = {
        ...(categoryId ? { categoryId } : {}),
        ...(search
          ? {
              OR: [
                { name: { contains: search, mode: "insensitive" } },
                { description: { contains: search, mode: "insensitive" } },
              ],
            }
          : {}),
      };

      const [items, total] = await Promise.all([
        prisma.product.findMany({
          where,
          skip: (page - 1) * limit,
          take: limit,
          select: {
            id: true,
            name: true,
            description: true,
            price: true,
            discountPrice: true,
            status: true,
            stock: true,
            thumbnailUrl: true,
            lowestPrice: true,
            sellerCount: true,
            reviewCount: true,
            averageRating: true,
            popularityScore: true,
            category: {
              select: {
                id: true,
                name: true,
              },
            },
          },
          orderBy: { id: "asc" },
        }),
        prisma.product.count({ where }),
      ]);

      res.status(200).json(success(items, { page, limit, total }));
    }),
  );

  router.get(
    `${apiPrefix}/products/:id`,
    asyncHandler(async (req, res) => {
      const item = await prisma.product.findUnique({
        where: { id: Number(req.params.id) },
        include: {
          category: {
            select: { id: true, name: true, parentId: true },
          },
          options: {
            select: { id: true, name: true, values: true, createdAt: true, updatedAt: true },
            orderBy: { id: "asc" },
          },
          images: {
            select: { id: true, url: true, isMain: true, sortOrder: true, createdAt: true },
            orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
          },
          specs: {
            select: {
              id: true,
              value: true,
              numericValue: true,
              specDefinition: {
                select: { id: true, name: true, unit: true, dataType: true },
              },
            },
            orderBy: { id: "asc" },
          },
        },
      });

      if (!item) {
        throw notFound("Product not found");
      }

      res.status(200).json(success(item));
    }),
  );

  router.get(
    `${apiPrefix}/specs/definitions`,
    asyncHandler(async (req, res) => {
      const categoryId = req.query.categoryId ? Number(req.query.categoryId) : undefined;
      const items = await prisma.specDefinition.findMany({
        where: categoryId ? { categoryId } : {},
        orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.get(
    `${apiPrefix}/products/:id/specs`,
    asyncHandler(async (req, res) => {
      const items = await prisma.productSpec.findMany({
        where: { productId: Number(req.params.id) },
        include: {
          specDefinition: {
            select: {
              id: true,
              name: true,
              unit: true,
              dataType: true,
              isComparable: true,
            },
          },
        },
        orderBy: { id: "asc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.get(
    `${apiPrefix}/products/:id/prices`,
    asyncHandler(async (req, res) => {
      const items = await prisma.priceEntry.findMany({
        where: { productId: Number(req.params.id) },
        include: {
          seller: {
            select: {
              id: true,
              name: true,
              trustScore: true,
              trustGrade: true,
              isActive: true,
            },
          },
        },
        orderBy: [{ price: "asc" }, { id: "asc" }],
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.get(
    `${apiPrefix}/products/:id/price-history`,
    asyncHandler(async (req, res) => {
      const items = await prisma.priceHistory.findMany({
        where: { productId: Number(req.params.id) },
        orderBy: { date: "desc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  return router;
}
