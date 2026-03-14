import { Router } from "express";

import { prisma } from "../prisma.js";
import { asyncHandler } from "../utils/async-handler.js";
import { failure, success } from "../utils/response.js";

const router = Router();

router.get(
  "/health",
  asyncHandler(async (_req, res) => {
    await prisma.$queryRaw`SELECT 1`;
    res.status(200).json(
      success({
        status: "UP",
        app: process.env.APP_NAME ?? "javascript-express-prisma",
        database: "reachable",
      }),
    );
  }),
);

export function createHealthRoutes(apiPrefix) {
  const prefixed = Router();

  prefixed.get(
    `${apiPrefix}/health`,
    asyncHandler(async (_req, res) => {
      await prisma.$queryRaw`SELECT 1`;
      res.status(200).json(success({ status: "UP" }));
    }),
  );

  prefixed.get(
    `${apiPrefix}/docs-status`,
    (_req, res) => {
      res.status(200).json(
        success({
          swagger: "pending",
          openapi: "pending",
          message: "Swagger/OpenAPI wiring is the next setup step.",
        }),
      );
    },
  );

  prefixed.use((err, _req, res, _next) => {
    res.status(503).json(
      failure(
        "DATABASE_UNREACHABLE",
        err instanceof Error ? err.message : "unknown error",
      ),
    );
  });

  return { router, prefixed };
}
