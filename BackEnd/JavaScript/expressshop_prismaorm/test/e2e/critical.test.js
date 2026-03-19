import assert from "node:assert/strict";
import test, { after, before } from "node:test";

import {
  bootTestApp,
  requestJson,
  requestText,
  shutdownTestApp,
  withBearer,
} from "./_support/harness.js";

let context;

before(async () => {
  context = await bootTestApp();
});

after(async () => {
  await shutdownTestApp(context);
});

test("GET /health returns UP", async () => {
  const result = await requestJson(context.baseUrl, "/health");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.status, "UP");
});

test("GET /api/v1/docs-status returns available docs wiring state", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/docs-status");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.swagger, "available");
  assert.equal(result.body.data.openapi, "available");
  assert.equal(result.body.data.openapiPath, "/docs/openapi");
  assert.equal(result.body.data.swaggerPath, "/docs/swagger");
});

test("GET /docs/openapi exposes OpenAPI document", async () => {
  const result = await requestJson(context.baseUrl, "/docs/openapi");

  assert.equal(result.status, 200);
  assert.equal(typeof result.body.openapi, "string");
  assert.equal(result.body.info.title, "PBShop JavaScript Express Prisma API");
  assert.ok(result.body.paths["/api/v1/products"]);
  assert.ok(result.body.paths["/api/v1/errors/codes"]);
  assert.ok(result.body.paths["/api/v1/errors/codes/{key}"]);
});

test("GET /docs/swagger exposes Swagger UI page", async () => {
  const result = await requestText(context.baseUrl, "/docs/swagger");

  assert.equal(result.status, 200);
  assert.match(result.body, /Swagger UI/);
});

test("GET /api/v1/products returns public product list", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/products");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
});

test("GET /api/v1/errors/codes returns public error code catalog", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/errors/codes");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.total, "number");
  assert.ok(Array.isArray(result.body.data.items));
  assert.ok(result.body.data.items.length > 0);
  assert.equal(typeof result.body.data.items[0].key, "string");
  assert.equal(typeof result.body.data.items[0].code, "string");
  assert.equal(typeof result.body.data.items[0].message, "string");
});

test("GET /api/v1/errors/codes/:key returns a matching error code item", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/errors/codes/NOT_FOUND");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.key, "NOT_FOUND");
  assert.equal(result.body.data.code, "HTTP_404");
  assert.equal(typeof result.body.data.message, "string");
});

test("GET /api/v1/errors/codes/:key returns null for unknown keys", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/errors/codes/UNKNOWN_CODE");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data, null);
});

test("GET /api/v1/users/me requires auth", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/users/me");

  assert.equal(result.status, 401);
  assert.equal(result.body.success, false);
});

test("GET /api/v1/users/me returns current user with bearer token", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/users/me",
    withBearer(context.userToken),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.role, "USER");
});

test("GET /api/v1/admin/observability/metrics rejects non-admin user", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/observability/metrics",
    withBearer(context.userToken),
  );

  assert.equal(result.status, 403);
  assert.equal(result.body.success, false);
});

test("GET /api/v1/admin/observability/metrics allows admin user", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/observability/metrics",
    withBearer(context.adminToken),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.totalRequests, "number");
});

test("GET /api/v1/pc-builds returns current user build list", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/pc-builds?page=1&limit=5",
    withBearer(context.userToken),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
  assert.equal(typeof result.body.meta.total, "number");
});

test("GET /api/v1/images/:id/variants returns 404 for missing image", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/images/999999/variants");

  assert.equal(result.status, 404);
  assert.equal(result.body.success, false);
});
