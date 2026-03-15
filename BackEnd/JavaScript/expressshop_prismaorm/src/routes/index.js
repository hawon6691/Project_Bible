import { Router } from "express";

import { apiHealthController, docsStatusController, healthController } from "../health/health.controller.js";
import addressesRouter from "./addresses.js";
import authRouter from "./auth.js";
import boardsRouter from "./boards.js";
import categoriesRouter from "./categories.js";
import cartRouter from "./cart.js";
import faqsRouter from "./faqs.js";
import inquiriesRouter from "./inquiries.js";
import noticesRouter from "./notices.js";
import ordersRouter from "./orders.js";
import paymentsRouter from "./payments.js";
import pointsRouter from "./points.js";
import productsRouter from "./products.js";
import reviewsRouter from "./reviews.js";
import ticketsRouter from "./tickets.js";
import usersRouter from "./users.js";
import wishlistRouter from "./wishlist.js";
import { asyncHandler } from "../utils/async-handler.js";

export function createRoutes(apiPrefix) {
  const router = Router();

  router.get("/health", asyncHandler(healthController));
  router.get(`${apiPrefix}/health`, asyncHandler(apiHealthController));
  router.get(`${apiPrefix}/docs-status`, docsStatusController);

  router.use(apiPrefix, authRouter);
  router.use(apiPrefix, usersRouter);
  router.use(apiPrefix, categoriesRouter);
  router.use(apiPrefix, productsRouter);
  router.use(apiPrefix, cartRouter);
  router.use(apiPrefix, addressesRouter);
  router.use(apiPrefix, ordersRouter);
  router.use(apiPrefix, paymentsRouter);
  router.use(apiPrefix, reviewsRouter);
  router.use(apiPrefix, wishlistRouter);
  router.use(apiPrefix, pointsRouter);
  router.use(apiPrefix, boardsRouter);
  router.use(apiPrefix, inquiriesRouter);
  router.use(apiPrefix, faqsRouter);
  router.use(apiPrefix, noticesRouter);
  router.use(apiPrefix, ticketsRouter);

  return router;
}
