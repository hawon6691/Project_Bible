import { Router } from "express";

import { prisma } from "../prisma.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";
import { badRequest, notFound } from "../utils/http-error.js";
import { success } from "../utils/response.js";

export function createSupportRoutes(apiPrefix) {
  const router = Router();

  router.get(
    `${apiPrefix}/support/tickets`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const items = await prisma.supportTicket.findMany({
        where: { userId: req.user.id },
        orderBy: { id: "desc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.post(
    `${apiPrefix}/support/tickets`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const { category, title, content, attachmentUrls } = req.body ?? {};
      if (!category || !title || !content) {
        throw badRequest("category, title, content are required");
      }

      const created = await prisma.supportTicket.create({
        data: {
          ticketNumber: `TCK-${Date.now()}`,
          userId: req.user.id,
          category,
          title,
          content,
          attachmentUrls: attachmentUrls ?? null,
        },
      });

      res.status(201).json(success(created));
    }),
  );

  router.get(
    `${apiPrefix}/support/tickets/:id`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const ticket = await prisma.supportTicket.findFirst({
        where: { id: Number(req.params.id), userId: req.user.id },
        include: {
          replies: {
            include: {
              user: {
                select: { id: true, name: true, role: true },
              },
            },
            orderBy: { id: "asc" },
          },
        },
      });

      if (!ticket) {
        throw notFound("Support ticket not found");
      }

      res.status(200).json(success(ticket));
    }),
  );

  router.post(
    `${apiPrefix}/support/tickets/:id/reply`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const { content } = req.body ?? {};
      if (!content) {
        throw badRequest("content is required");
      }

      const ticketId = Number(req.params.id);
      const ticket = await prisma.supportTicket.findUnique({
        where: { id: ticketId },
      });

      if (!ticket) {
        throw notFound("Support ticket not found");
      }

      if (ticket.userId !== req.user.id && req.user.role !== "ADMIN") {
        throw notFound("Support ticket not found");
      }

      const created = await prisma.ticketReply.create({
        data: {
          ticketId,
          userId: req.user.id,
          content,
          isAdmin: req.user.role === "ADMIN",
        },
      });

      res.status(201).json(success(created));
    }),
  );

  router.get(
    `${apiPrefix}/admin/support/tickets`,
    requireAuth,
    requireRole("ADMIN"),
    asyncHandler(async (_req, res) => {
      const items = await prisma.supportTicket.findMany({
        include: {
          user: {
            select: { id: true, email: true, name: true },
          },
        },
        orderBy: { id: "desc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.patch(
    `${apiPrefix}/admin/support/tickets/:id/status`,
    requireAuth,
    requireRole("ADMIN"),
    asyncHandler(async (req, res) => {
      const { status } = req.body ?? {};
      if (!status) {
        throw badRequest("status is required");
      }

      const updated = await prisma.supportTicket.update({
        where: { id: Number(req.params.id) },
        data: { status },
      });

      res.status(200).json(success(updated));
    }),
  );

  return router;
}
