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

const QUERY_PRODUCT_VIEW_SCHEMA = {
  type: "object",
  required: [
    "productId",
    "categoryId",
    "name",
    "status",
    "basePrice",
    "lowestPrice",
    "sellerCount",
    "averageRating",
    "reviewCount",
    "viewCount",
    "popularityScore",
    "syncedAt",
    "updatedAt",
  ],
  properties: {
    productId: { type: "integer", example: 1 },
    categoryId: { type: "integer", example: 10 },
    name: { type: "string", example: "PBShop Gaming Laptop" },
    thumbnailUrl: { type: ["string", "null"], example: "https://cdn.pbshop.dev/products/1.png" },
    status: { type: "string", example: "ON_SALE" },
    basePrice: { type: "integer", example: 1599000 },
    lowestPrice: { type: ["integer", "null"], example: 1499000 },
    sellerCount: { type: "integer", example: 3 },
    averageRating: { type: "number", example: 4.7 },
    reviewCount: { type: "integer", example: 28 },
    viewCount: { type: "integer", example: 1024 },
    popularityScore: { type: "number", example: 86.4 },
    syncedAt: { type: "string", format: "date-time" },
    updatedAt: { type: "string", format: "date-time" },
  },
};

const CHAT_ROOM_SCHEMA = {
  type: "object",
  required: ["id", "name", "createdBy", "isPrivate", "createdAt", "updatedAt"],
  properties: {
    id: { type: "integer", example: 1 },
    name: { type: "string", example: "고객 문의 채팅" },
    createdBy: { type: "integer", example: 2 },
    isPrivate: { type: "boolean", example: true },
    createdAt: { type: "string", format: "date-time" },
    updatedAt: { type: "string", format: "date-time" },
  },
};

const CHAT_MESSAGE_SCHEMA = {
  type: "object",
  required: ["id", "roomId", "senderId", "message", "createdAt", "updatedAt"],
  properties: {
    id: { type: "integer", example: 10 },
    roomId: { type: "integer", example: 1 },
    senderId: { type: "integer", example: 2 },
    message: { type: "string", example: "안녕하세요. 상품 재고 문의드립니다." },
    createdAt: { type: "string", format: "date-time" },
    updatedAt: { type: "string", format: "date-time" },
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

function buildQueryProductListResponseSchema() {
  return {
    allOf: [
      SUCCESS_SCHEMA,
      {
        type: "object",
        properties: {
          data: {
            type: "array",
            items: {
              $ref: "#/components/schemas/QueryProductView",
            },
          },
          meta: {
            type: "object",
            required: ["page", "limit", "total", "totalPages"],
            properties: {
              page: { type: "integer", minimum: 1, example: 1 },
              limit: { type: "integer", minimum: 1, example: 20 },
              total: { type: "integer", minimum: 0, example: 1 },
              totalPages: { type: "integer", minimum: 0, example: 1 },
            },
          },
        },
      },
    ],
  };
}

function buildSingleQueryProductResponseSchema() {
  return {
    allOf: [
      SUCCESS_SCHEMA,
      {
        type: "object",
        properties: {
          data: {
            $ref: "#/components/schemas/QueryProductView",
          },
        },
      },
    ],
  };
}

function buildRebuildQueryProductsResponseSchema() {
  return {
    allOf: [
      SUCCESS_SCHEMA,
      {
        type: "object",
        properties: {
          data: {
            type: "object",
            required: ["syncedCount"],
            properties: {
              syncedCount: { type: "integer", minimum: 0, example: 42 },
            },
          },
        },
      },
    ],
  };
}

function buildSingleChatRoomResponseSchema() {
  return {
    allOf: [
      SUCCESS_SCHEMA,
      {
        type: "object",
        properties: {
          data: {
            $ref: "#/components/schemas/ChatRoom",
          },
        },
      },
    ],
  };
}

function buildSingleChatMessageResponseSchema() {
  return {
    allOf: [
      SUCCESS_SCHEMA,
      {
        type: "object",
        properties: {
          data: {
            $ref: "#/components/schemas/ChatMessage",
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

  if (route.path === "/api/v1/query/products" && route.method === "GET") {
    operation.responses[200] = {
      description: "Query product list",
      content: {
        "application/json": {
          schema: buildQueryProductListResponseSchema(),
        },
      },
    };
  }

  if (route.path === "/api/v1/query/products/{productId}" && route.method === "GET") {
    operation.responses[200] = {
      description: "Single query product view",
      content: {
        "application/json": {
          schema: buildSingleQueryProductResponseSchema(),
        },
      },
    };
  }

  if (route.path === "/api/v1/admin/query/products/{productId}/sync" && route.method === "POST") {
    operation.responses[200] = {
      description: "Sync a single query product view",
      content: {
        "application/json": {
          schema: buildSingleQueryProductResponseSchema(),
        },
      },
    };
  }

  if (route.path === "/api/v1/admin/query/products/rebuild" && route.method === "POST") {
    operation.responses[200] = {
      description: "Rebuild all query product views",
      content: {
        "application/json": {
          schema: buildRebuildQueryProductsResponseSchema(),
        },
      },
    };
  }

  if (route.path === "/api/v1/chat/rooms/{id}/join" && route.method === "POST") {
    operation.responses[200] = {
      description: "Join a chat room",
      content: {
        "application/json": {
          schema: buildSingleChatRoomResponseSchema(),
        },
      },
    };
  }

  if (route.path === "/api/v1/chat/rooms/{id}/messages" && route.method === "POST") {
    operation.responses[201] = {
      description: "Send a chat message",
      content: {
        "application/json": {
          schema: buildSingleChatMessageResponseSchema(),
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
        ChatMessage: CHAT_MESSAGE_SCHEMA,
        ChatRoom: CHAT_ROOM_SCHEMA,
        ErrorCodeItem: ERROR_CODE_ITEM_SCHEMA,
        QueryProductView: QUERY_PRODUCT_VIEW_SCHEMA,
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
