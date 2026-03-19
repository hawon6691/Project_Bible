import { getRouteCatalog } from "../routes/index.js";

const ERROR_CODE_ITEM_SCHEMA = {
  type: "object",
  required: ["key", "code", "message"],
  properties: {
    key: { type: "string", example: "NOT_FOUND" },
    code: { type: "string", example: "HTTP_404" },
    message: {
      type: "string",
      example: "The requested resource could not be found.",
    },
  },
};

const SUCCESS_SCHEMA = {
  type: "object",
  required: ["success", "data"],
  properties: {
    success: { type: "boolean", const: true },
    data: {},
    meta: {
      type: "object",
      additionalProperties: true,
    },
  },
};

function buildErrorCodeResponseSchema() {
  return {
    allOf: [
      SUCCESS_SCHEMA,
      {
        type: "object",
        properties: {
          data: {
            type: "object",
            required: ["total", "items"],
            properties: {
              total: { type: "integer", minimum: 0, example: 8 },
              items: {
                type: "array",
                items: {
                  $ref: "#/components/schemas/ErrorCodeItem",
                },
              },
            },
          },
        },
      },
    ],
  };
}

function buildSingleErrorCodeResponseSchema() {
  return {
    allOf: [
      SUCCESS_SCHEMA,
      {
        type: "object",
        properties: {
          data: {
            oneOf: [
              { $ref: "#/components/schemas/ErrorCodeItem" },
              { type: "null" },
            ],
          },
        },
      },
    ],
  };
}

function buildOperation(route) {
  const operation = {
    tags: [buildTag(route.path)],
    summary: buildSummary(route.method, route.path),
    responses: {
      200: {
        description: "Successful response",
      },
    },
  };

  if (route.path === "/api/v1/errors/codes" && route.method === "GET") {
    operation.responses[200] = {
      description: "Error code catalog",
      content: {
        "application/json": {
          schema: buildErrorCodeResponseSchema(),
        },
      },
    };
  }

  if (route.path === "/api/v1/errors/codes/{key}" && route.method === "GET") {
    operation.responses[200] = {
      description: "Single error code lookup",
      content: {
        "application/json": {
          schema: buildSingleErrorCodeResponseSchema(),
        },
      },
    };
  }

  return operation;
}

function buildSummary(method, path) {
  const normalizedPath = path.replace(/^\/api\/v1/, "") || "/";
  return `${method} ${normalizedPath}`;
}

function buildTag(path) {
  const segments = path.split("/").filter(Boolean);
  if (segments.length === 0) {
    return "root";
  }
  if (segments[0] === "api" && segments[1] === "v1") {
    return segments[2] ?? "api";
  }
  return segments[0];
}

export function buildOpenApiSpec(apiPrefix) {
  const routes = getRouteCatalog(apiPrefix);
  const paths = {};

  for (const route of routes) {
    if (!paths[route.path]) {
      paths[route.path] = {};
    }

    paths[route.path][route.method.toLowerCase()] = buildOperation(route);
  }

  return {
    openapi: "3.1.0",
    info: {
      title: "PBShop JavaScript Express Prisma API",
      version: "0.1.0",
      description:
        "Shared-document-based JavaScript Express Prisma backend for PBShop.",
    },
    servers: [
      {
        url: `http://localhost:${process.env.PORT ?? 8000}`,
        description: "Local server",
      },
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: "http",
          scheme: "bearer",
          bearerFormat: "JWT",
        },
      },
      schemas: {
        ErrorCodeItem: ERROR_CODE_ITEM_SCHEMA,
      },
    },
    paths,
  };
}

export function renderSwaggerHtml() {
  return `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Swagger UI</title>
    <link
      rel="stylesheet"
      href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css"
    />
    <style>
      body {
        margin: 0;
        background: #f5f7fb;
        font-family: Arial, sans-serif;
      }

      .fallback {
        max-width: 960px;
        margin: 24px auto;
        padding: 0 16px;
        color: #334155;
      }
    </style>
  </head>
  <body>
    <div id="swagger-ui"></div>
    <div id="fallback" class="fallback" style="display:none;">
      <h1>Swagger UI</h1>
      <p>Swagger assets could not be loaded from the CDN.</p>
      <p>
        You can still inspect the OpenAPI document at
        <a href="/docs/openapi">/docs/openapi</a>.
      </p>
    </div>
    <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
    <script>
      if (window.SwaggerUIBundle) {
        window.SwaggerUIBundle({
          url: "/docs/openapi",
          dom_id: "#swagger-ui",
          deepLinking: true,
          displayRequestDuration: true,
        });
      } else {
        document.getElementById("fallback").style.display = "block";
      }
    </script>
  </body>
</html>`;
}
