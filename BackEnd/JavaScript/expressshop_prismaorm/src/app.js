import cors from "cors";
import express from "express";
import dotenv from "dotenv";

import { createAuthRoutes } from "./routes/auth-routes.js";
import { createCatalogRoutes } from "./routes/catalog-routes.js";
import { createCommerceRoutes } from "./routes/commerce-routes.js";
import { createCommunityRoutes } from "./routes/community-routes.js";
import { createEngagementRoutes } from "./routes/engagement-routes.js";
import { createHealthRoutes } from "./routes/health-routes.js";
import { createSupportRoutes } from "./routes/support-routes.js";
import { createUserRoutes } from "./routes/user-routes.js";
import { HttpError } from "./utils/http-error.js";
import { failure } from "./utils/response.js";

export function createApp() {
  dotenv.config();
  const app = express();
  const apiPrefix = process.env.API_PREFIX ?? "/api/v1";

  app.use(cors());
  app.use(express.json());

  const healthRoutes = createHealthRoutes(apiPrefix);
  app.use(healthRoutes.router);
  app.use(healthRoutes.prefixed);
  app.use(createAuthRoutes(apiPrefix));
  app.use(createUserRoutes(apiPrefix));
  app.use(createCatalogRoutes(apiPrefix));
  app.use(createCommerceRoutes(apiPrefix));
  app.use(createEngagementRoutes(apiPrefix));
  app.use(createCommunityRoutes(apiPrefix));
  app.use(createSupportRoutes(apiPrefix));

  app.use((_req, res) => {
    res.status(404).json(failure("NOT_FOUND", "Route not found"));
  });

  app.use((err, _req, res, _next) => {
    if (err instanceof HttpError) {
      res
        .status(err.status)
        .json(failure(err.code, err.message, err.details));
      return;
    }

    res
      .status(500)
      .json(
        failure(
          "INTERNAL_SERVER_ERROR",
          err instanceof Error ? err.message : "unexpected error",
        ),
      );
  });

  return app;
}
