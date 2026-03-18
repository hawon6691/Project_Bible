import { getRouteCatalog } from "../routes/index.js";

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

    paths[route.path][route.method.toLowerCase()] = {
      tags: [buildTag(route.path)],
      summary: buildSummary(route.method, route.path),
      responses: {
        200: {
          description: "Successful response",
        },
      },
    };
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
