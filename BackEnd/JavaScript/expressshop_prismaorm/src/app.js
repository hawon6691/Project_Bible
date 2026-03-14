import cors from "cors";
import express from "express";
import dotenv from "dotenv";

import { prisma } from "./prisma.js";

dotenv.config();

function success(data, meta) {
  return {
    success: true,
    data,
    ...(meta ? { meta } : {}),
  };
}

function failure(code, message, details) {
  return {
    success: false,
    error: {
      code,
      message,
      ...(details ? { details } : {}),
    },
  };
}

export function createApp() {
  const app = express();
  const apiPrefix = process.env.API_PREFIX ?? "/api/v1";

  app.use(cors());
  app.use(express.json());

  app.get("/health", async (_req, res) => {
    try {
      await prisma.$queryRaw`SELECT 1`;
      res.status(200).json(
        success({
          status: "UP",
          app: process.env.APP_NAME ?? "javascript-express-prisma",
          database: "reachable",
        }),
      );
    } catch (error) {
      res.status(503).json(
        failure(
          "DATABASE_UNREACHABLE",
          error instanceof Error ? error.message : "unknown error",
        ),
      );
    }
  });

  app.get(`${apiPrefix}/health`, async (_req, res) => {
    try {
      await prisma.$queryRaw`SELECT 1`;
      res.status(200).json(success({ status: "UP" }));
    } catch (error) {
      res.status(503).json(
        failure(
          "DATABASE_UNREACHABLE",
          error instanceof Error ? error.message : "unknown error",
        ),
      );
    }
  });

  app.get(`${apiPrefix}/categories`, async (_req, res, next) => {
    try {
      const items = await prisma.category.findMany({
        orderBy: { id: "asc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    } catch (error) {
      next(error);
    }
  });

  app.get(`${apiPrefix}/products`, async (_req, res, next) => {
    try {
      const items = await prisma.product.findMany({
        include: {
          category: {
            select: {
              id: true,
              name: true,
              slug: true,
            },
          },
        },
        orderBy: { id: "asc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    } catch (error) {
      next(error);
    }
  });

  app.get(`${apiPrefix}/docs-status`, (_req, res) => {
    res.status(200).json(
      success({
        swagger: "pending",
        openapi: "pending",
        message: "Swagger/OpenAPI wiring is the next setup step.",
      }),
    );
  });

  app.use((_req, res) => {
    res.status(404).json(failure("NOT_FOUND", "Route not found"));
  });

  app.use((err, _req, res, _next) => {
    res.status(500).json(
      failure(
        "INTERNAL_SERVER_ERROR",
        err instanceof Error ? err.message : "unexpected error",
      ),
    );
  });

  return app;
}
