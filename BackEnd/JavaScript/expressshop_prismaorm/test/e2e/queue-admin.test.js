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

test("GET /api/v1/admin/queues/supported returns queue names", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/queues/supported",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data.items));
  assert.ok(result.body.data.items.length > 0);
});

test("GET /api/v1/admin/queues/stats returns aggregate queue stats", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/queues/stats",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.total, "number");
  assert.ok(Array.isArray(result.body.data.items));
});

test("GET /api/v1/admin/queues/:queueName/failed returns failed jobs list", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/queues/crawler-collect/failed?page=1&limit=5",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data.items));
  assert.equal(typeof result.body.data.total, "number");
});

test("POST /api/v1/admin/queues/auto-retry returns retry summary", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/queues/auto-retry?perQueueLimit=1&maxTotal=2",
    {
      ...asAdmin(),
      method: "POST",
    },
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.retriedTotal, "number");
  assert.ok(Array.isArray(result.body.data.items));
});
