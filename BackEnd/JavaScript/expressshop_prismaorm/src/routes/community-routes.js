import { Router } from "express";

import { prisma } from "../prisma.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";
import { badRequest, notFound } from "../utils/http-error.js";
import { success } from "../utils/response.js";

export function createCommunityRoutes(apiPrefix) {
  const router = Router();

  router.get(
    `${apiPrefix}/community/boards`,
    asyncHandler(async (_req, res) => {
      const items = await prisma.board.findMany({
        where: { isActive: true },
        orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.get(
    `${apiPrefix}/community/posts`,
    asyncHandler(async (req, res) => {
      const boardId = req.query.boardId ? Number(req.query.boardId) : undefined;
      const items = await prisma.post.findMany({
        where: {
          ...(boardId ? { boardId } : {}),
          deletedAt: null,
        },
        include: {
          board: {
            select: { id: true, name: true, slug: true },
          },
          user: {
            select: { id: true, name: true, nickname: true },
          },
        },
        orderBy: { id: "desc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.post(
    `${apiPrefix}/community/posts`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const { boardId, title, content } = req.body ?? {};
      if (!boardId || !title || !content) {
        throw badRequest("boardId, title, content are required");
      }

      const created = await prisma.post.create({
        data: {
          boardId: Number(boardId),
          userId: req.user.id,
          title,
          content,
        },
      });

      res.status(201).json(success(created));
    }),
  );

  router.get(
    `${apiPrefix}/community/posts/:id`,
    asyncHandler(async (req, res) => {
      const postId = Number(req.params.id);
      const post = await prisma.post.findUnique({
        where: { id: postId },
        include: {
          board: true,
          user: {
            select: { id: true, name: true, nickname: true },
          },
          comments: {
            where: { deletedAt: null },
            include: {
              user: {
                select: { id: true, name: true, nickname: true },
              },
            },
            orderBy: { id: "asc" },
          },
        },
      });

      if (!post || post.deletedAt) {
        throw notFound("Post not found");
      }

      await prisma.post.update({
        where: { id: post.id },
        data: { viewCount: { increment: 1 } },
      });

      res.status(200).json(success(post));
    }),
  );

  router.post(
    `${apiPrefix}/community/posts/:id/comments`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const { content, parentId } = req.body ?? {};
      if (!content) {
        throw badRequest("content is required");
      }

      const created = await prisma.comment.create({
        data: {
          postId: Number(req.params.id),
          userId: req.user.id,
          parentId: parentId ? Number(parentId) : null,
          content,
        },
      });

      await prisma.post.update({
        where: { id: Number(req.params.id) },
        data: { commentCount: { increment: 1 } },
      });

      res.status(201).json(success(created));
    }),
  );

  router.get(
    `${apiPrefix}/products/:productId/inquiries`,
    asyncHandler(async (req, res) => {
      const items = await prisma.inquiry.findMany({
        where: { productId: Number(req.params.productId) },
        include: {
          user: {
            select: { id: true, name: true, nickname: true },
          },
          answeredUser: {
            select: { id: true, name: true },
          },
        },
        orderBy: { id: "desc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.post(
    `${apiPrefix}/products/:productId/inquiries`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const { title, content, isSecret } = req.body ?? {};
      if (!title || !content) {
        throw badRequest("title and content are required");
      }

      const created = await prisma.inquiry.create({
        data: {
          productId: Number(req.params.productId),
          userId: req.user.id,
          title,
          content,
          isSecret: Boolean(isSecret),
        },
      });

      res.status(201).json(success(created));
    }),
  );

  router.get(
    `${apiPrefix}/faqs`,
    asyncHandler(async (req, res) => {
      const category = req.query.category ? String(req.query.category) : undefined;
      const search = String(req.query.search ?? "").trim();
      const items = await prisma.faq.findMany({
        where: {
          isActive: true,
          ...(category ? { category } : {}),
          ...(search
            ? {
                OR: [
                  { question: { contains: search, mode: "insensitive" } },
                  { answer: { contains: search, mode: "insensitive" } },
                ],
              }
            : {}),
        },
        orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.post(
    `${apiPrefix}/faqs`,
    requireAuth,
    requireRole("ADMIN"),
    asyncHandler(async (req, res) => {
      const { category, question, answer, sortOrder, isActive } = req.body ?? {};
      if (!category || !question || !answer) {
        throw badRequest("category, question, answer are required");
      }

      const created = await prisma.faq.create({
        data: {
          category,
          question,
          answer,
          sortOrder: Number(sortOrder ?? 0),
          isActive: typeof isActive === "boolean" ? isActive : true,
        },
      });

      res.status(201).json(success(created));
    }),
  );

  router.get(
    `${apiPrefix}/notices`,
    asyncHandler(async (req, res) => {
      const page = Number(req.query.page ?? 1);
      const limit = Math.min(Number(req.query.limit ?? 20), 100);
      const [items, total] = await Promise.all([
        prisma.notice.findMany({
          skip: (page - 1) * limit,
          take: limit,
          orderBy: [{ isPinned: "desc" }, { createdAt: "desc" }],
        }),
        prisma.notice.count(),
      ]);

      res.status(200).json(success(items, { page, limit, total }));
    }),
  );

  router.get(
    `${apiPrefix}/notices/:id`,
    asyncHandler(async (req, res) => {
      const noticeId = Number(req.params.id);
      const notice = await prisma.notice.findUnique({
        where: { id: noticeId },
      });

      if (!notice) {
        throw notFound("Notice not found");
      }

      const updated = await prisma.notice.update({
        where: { id: noticeId },
        data: { viewCount: { increment: 1 } },
      });

      res.status(200).json(success(updated));
    }),
  );

  return router;
}
