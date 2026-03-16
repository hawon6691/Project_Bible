import { Router } from "express";

import { apiHealthController, docsStatusController, healthController } from "../health/health.controller.js";
import addressesRouter from "./addresses.js";
import activitiesRouter from "./activities.js";
import authRouter from "./auth.js";
import boardsRouter from "./boards.js";
import categoriesRouter from "./categories.js";
import cartRouter from "./cart.js";
import chatsRouter from "./chats.js";
import dealsRouter from "./deals.js";
import faqsRouter from "./faqs.js";
import friendsRouter from "./friends.js";
import inquiriesRouter from "./inquiries.js";
import matchingRouter from "./matching.js";
import mediaRouter from "./media.js";
import newsRouter from "./news.js";
import noticesRouter from "./notices.js";
import ordersRouter from "./orders.js";
import paymentsRouter from "./payments.js";
import pointsRouter from "./points.js";
import productsRouter from "./products.js";
import rankingsRouter from "./rankings.js";
import recommendationsRouter from "./recommendations.js";
import reviewsRouter from "./reviews.js";
import sellersRouter from "./sellers.js";
import shortformsRouter from "./shortforms.js";
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
  router.use(apiPrefix, sellersRouter);
  router.use(apiPrefix, cartRouter);
  router.use(apiPrefix, addressesRouter);
  router.use(apiPrefix, activitiesRouter);
  router.use(apiPrefix, chatsRouter);
  router.use(apiPrefix, friendsRouter);
  router.use(apiPrefix, shortformsRouter);
  router.use(apiPrefix, mediaRouter);
  router.use(apiPrefix, newsRouter);
  router.use(apiPrefix, matchingRouter);
  router.use(apiPrefix, rankingsRouter);
  router.use(apiPrefix, recommendationsRouter);
  router.use(apiPrefix, dealsRouter);
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
