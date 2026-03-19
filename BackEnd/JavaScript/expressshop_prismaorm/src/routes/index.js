import { Router } from "express";

import { apiHealthController, docsStatusController, healthController } from "../health/health.controller.js";
import addressesRouter from "./addresses.js";
import activitiesRouter from "./activities.js";
import adminSettingsRouter from "./admin-settings.js";
import analyticsRouter from "./analytics.js";
import autoRouter from "./auto.js";
import authRouter from "./auth.js";
import auctionsRouter from "./auctions.js";
import badgesRouter from "./badges.js";
import boardsRouter from "./boards.js";
import categoriesRouter from "./categories.js";
import cartRouter from "./cart.js";
import chatsRouter from "./chats.js";
import compareRouter from "./compare.js";
import crawlerRouter from "./crawler.js";
import dealsRouter from "./deals.js";
import errorsRouter from "./errors.js";
import faqsRouter from "./faqs.js";
import friendsRouter from "./friends.js";
import fraudRouter from "./fraud.js";
import i18nRouter from "./i18n.js";
import imagesRouter from "./images.js";
import inquiriesRouter from "./inquiries.js";
import matchingRouter from "./matching.js";
import mediaRouter from "./media.js";
import newsRouter from "./news.js";
import noticesRouter from "./notices.js";
import observabilityRouter from "./observability.js";
import opsDashboardRouter from "./ops-dashboard.js";
import ordersRouter from "./orders.js";
import paymentsRouter from "./payments.js";
import pcBuildsRouter from "./pc-builds.js";
import pointsRouter from "./points.js";
import predictionsRouter from "./predictions.js";
import productsRouter from "./products.js";
import pushRouter from "./push.js";
import queueAdminRouter from "./queue-admin.js";
import rankingsRouter from "./rankings.js";
import recommendationsRouter from "./recommendations.js";
import resilienceRouter from "./resilience.js";
import reviewsRouter from "./reviews.js";
import searchRouter from "./search.js";
import sellersRouter from "./sellers.js";
import shortformsRouter from "./shortforms.js";
import ticketsRouter from "./tickets.js";
import usedMarketRouter from "./used-market.js";
import usersRouter from "./users.js";
import wishlistRouter from "./wishlist.js";
import { asyncHandler } from "../utils/async-handler.js";

function joinRoutePath(prefix, path) {
  const left = prefix.endsWith("/") ? prefix.slice(0, -1) : prefix;
  const right = path.startsWith("/") ? path : `/${path}`;
  const joined = `${left}${right}`.replace(/\/+/g, "/");
  return joined === "" ? "/" : joined;
}

function normalizeOpenApiPath(path) {
  return path.replace(/:([A-Za-z0-9_]+)/g, "{$1}");
}

function collectRouterRoutes(router, prefix) {
  const routes = [];

  for (const layer of router.stack ?? []) {
    if (layer.route?.path) {
      const routePaths = Array.isArray(layer.route.path)
        ? layer.route.path
        : [layer.route.path];
      const methods = Object.keys(layer.route.methods ?? {})
        .filter((method) => layer.route.methods[method])
        .map((method) => method.toUpperCase());

      for (const routePath of routePaths) {
        const fullPath = normalizeOpenApiPath(joinRoutePath(prefix, routePath));
        for (const method of methods) {
          routes.push({ method, path: fullPath });
        }
      }
      continue;
    }

    if (layer.handle?.stack) {
      routes.push(...collectRouterRoutes(layer.handle, prefix));
    }
  }

  return routes;
}

function dedupeRoutes(routes) {
  const seen = new Set();
  return routes.filter((route) => {
    const key = `${route.method}:${route.path}`;
    if (seen.has(key)) {
      return false;
    }
    seen.add(key);
    return true;
  });
}

const apiRouters = [
  authRouter,
  usersRouter,
  adminSettingsRouter,
  badgesRouter,
  analyticsRouter,
  autoRouter,
  auctionsRouter,
  categoriesRouter,
  productsRouter,
  fraudRouter,
  i18nRouter,
  imagesRouter,
  sellersRouter,
  pushRouter,
  queueAdminRouter,
  resilienceRouter,
  cartRouter,
  addressesRouter,
  activitiesRouter,
  chatsRouter,
  friendsRouter,
  shortformsRouter,
  mediaRouter,
  newsRouter,
  observabilityRouter,
  opsDashboardRouter,
  matchingRouter,
  compareRouter,
  crawlerRouter,
  rankingsRouter,
  recommendationsRouter,
  dealsRouter,
  errorsRouter,
  ordersRouter,
  paymentsRouter,
  pcBuildsRouter,
  predictionsRouter,
  reviewsRouter,
  searchRouter,
  wishlistRouter,
  pointsRouter,
  boardsRouter,
  inquiriesRouter,
  faqsRouter,
  noticesRouter,
  ticketsRouter,
  usedMarketRouter,
];

export function getRouteCatalog(apiPrefix) {
  const baseRoutes = [
    { method: "GET", path: "/health" },
    { method: "GET", path: normalizeOpenApiPath(`${apiPrefix}/health`) },
    { method: "GET", path: normalizeOpenApiPath(`${apiPrefix}/docs-status`) },
    { method: "GET", path: "/docs/openapi" },
    { method: "GET", path: "/docs/swagger" },
    { method: "GET", path: "/docs/swagger-ui/index.html" },
  ];

  const apiRoutes = apiRouters.flatMap((router) =>
    collectRouterRoutes(router, apiPrefix),
  );

  return dedupeRoutes([...baseRoutes, ...apiRoutes]).sort((a, b) => {
    if (a.path === b.path) {
      return a.method.localeCompare(b.method);
    }
    return a.path.localeCompare(b.path);
  });
}

export function createRoutes(apiPrefix) {
  const router = Router();

  router.get("/health", asyncHandler(healthController));
  router.get(`${apiPrefix}/health`, asyncHandler(apiHealthController));
  router.get(`${apiPrefix}/docs-status`, docsStatusController);

  for (const apiRouter of apiRouters) {
    router.use(apiPrefix, apiRouter);
  }

  return router;
}
