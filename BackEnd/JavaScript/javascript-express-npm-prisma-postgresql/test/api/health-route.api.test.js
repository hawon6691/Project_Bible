import assert from "node:assert/strict";
import test, { after, before } from "node:test";

import {
  bootTestApp,
  requestJson,
  shutdownTestApp,
} from "../e2e/_support/harness.js";

let context;

before(async () => {
  context = await bootTestApp();
});

after(async () => {
  await shutdownTestApp(context);
});

test("GET /health returns health payload", async () => {
  const result = await requestJson(context.baseUrl, "/health");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.status, "UP");
});

test("GET /api/v1/health returns API health payload", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/health");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.status, "UP");
});

test("GET /api/v1/docs-status returns docs wiring payload", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/docs-status");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.openapi, "available");
  assert.equal(result.body.data.swagger, "available");
});
