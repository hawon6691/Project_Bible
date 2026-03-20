import cors from "cors";
import express from "express";
import dotenv from "dotenv";

import { appConfig } from "./config/app.js";
import {
  openApiController,
  swaggerRedirectController,
  swaggerUiController,
} from "./docs/docs.controller.js";
import { getErrorCodeItem } from "./error-codes/error-code.catalog.js";
import { observabilityTrace } from "./middleware/observability.js";
import { createRoutes } from "./routes/index.js";
import { HttpError } from "./utils/http-error.js";
import { failure } from "./utils/response.js";

export function createApp() {
  dotenv.config();
  const app = express();

  app.use(cors());
  app.use(express.json());
  app.use(observabilityTrace);

  app.use(createRoutes(appConfig.apiPrefix));
  app.get("/docs/openapi", openApiController(appConfig.apiPrefix));
  app.get("/docs/swagger", swaggerRedirectController);
  app.get("/docs/swagger-ui/index.html", swaggerUiController);

  app.use((_req, res) => {
    const item = getErrorCodeItem("NOT_FOUND");
    res.status(404).json(failure("NOT_FOUND", item?.message ?? "Route not found"));
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
          err instanceof Error
            ? err.message
            : (getErrorCodeItem("INTERNAL_SERVER_ERROR")?.message ?? "unexpected error"),
        ),
      );
  });

  return app;
}
