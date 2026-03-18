import assert from "node:assert/strict";
import test, { after, before } from "node:test";

import {
  bootTestApp,
  requestJson,
  shutdownTestApp,
} from "./_support/harness.js";

let context;

before(async () => {
  context = await bootTestApp();
});

after(async () => {
  await shutdownTestApp(context);
});

test("GET /health matches public health contract", async () => {
  const result = await requestJson(context.baseUrl, "/health");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.status, "UP");
  assert.equal(typeof result.body.data.app, "string");
});

test("GET /api/v1/health matches api health contract", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/health");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.status, "UP");
});

test("GET /api/v1/docs-status matches docs status contract", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/docs-status");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.swagger, "available");
  assert.equal(result.body.data.openapi, "available");
});

test("GET /docs/openapi matches OpenAPI contract", async () => {
  const result = await requestJson(context.baseUrl, "/docs/openapi");

  assert.equal(result.status, 200);
  assert.equal(typeof result.body.openapi, "string");
  assert.equal(typeof result.body.info, "object");
  assert.equal(typeof result.body.paths, "object");
  assert.ok(result.body.paths["/api/v1/products"]);
});

test("GET /api/v1/categories matches categories list contract", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/categories");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
});

test("GET /api/v1/products matches products list contract", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/products");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
});

test("GET /api/v1/products/1 matches product detail contract", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/products/1");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.id, 1);
  assert.equal(typeof result.body.data.name, "string");
});

test("GET /api/v1/sellers matches sellers list contract", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/sellers");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
});

test("GET /api/v1/specs/definitions matches spec definitions contract", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/specs/definitions");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
});

test("GET /api/v1/i18n/exchange-rates matches exchange rate contract", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/i18n/exchange-rates");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
});

test("GET /api/v1/predictions/products/1/price-trend?days=7 matches prediction contract", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/predictions/products/1/price-trend?days=7",
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.productId, 1);
  assert.ok(Array.isArray(result.body.data.predictions));
  assert.equal(typeof result.body.data.currentPrice, "number");
});
