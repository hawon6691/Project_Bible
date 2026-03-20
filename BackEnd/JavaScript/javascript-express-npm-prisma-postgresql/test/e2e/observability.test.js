import assert from "node:assert/strict";
import test, { after, before } from "node:test";

import {
  bootTestApp,
  requestJson,
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

const asAdmin = () => withBearer(context.adminToken);

test("GET /api/v1/admin/observability/metrics returns metrics summary", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/observability/metrics",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.totalRequests, "number");
  assert.equal(typeof result.body.data.errorRate, "number");
});

test("GET /api/v1/admin/observability/traces returns trace list", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/observability/traces?limit=5",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data.items));
});

test("GET /api/v1/admin/observability/dashboard returns observability dashboard payload", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/observability/dashboard",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.checkedAt, "string");
  assert.equal(typeof result.body.data.metrics, "object");
});
