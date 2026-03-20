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

test("GET /api/v1/admin/queues/supported returns supported queues", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/queues/supported",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data.items));
  assert.ok(result.body.data.items.includes("crawler-collect"));
});

test("GET /api/v1/admin/queues/stats returns queue stats snapshot", async () => {
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

test("GET /api/v1/admin/queues/crawler-collect/failed returns failed jobs", async () => {
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

test("GET /api/v1/admin/queues/crawler-collect/failed rejects invalid pagination", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/queues/crawler-collect/failed?page=0&limit=5",
    asAdmin(),
  );

  assert.equal(result.status, 400);
  assert.equal(result.body.success, false);
  assert.equal(result.body.error.code, "BAD_REQUEST");
});

test("POST /api/v1/admin/queues/crawler-collect/jobs/crawler-failed-3001/retry requeues failed job", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/queues/crawler-collect/jobs/crawler-failed-3001/retry",
    {
      ...asAdmin(),
      method: "POST",
    },
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.retried, true);
});

test("POST /api/v1/admin/queues/video-transcode/jobs/completed-job/retry rejects non-failed job", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/queues/video-transcode/jobs/completed-job/retry",
    {
      ...asAdmin(),
      method: "POST",
    },
  );

  assert.equal(result.status, 400);
  assert.equal(result.body.success, false);
  assert.equal(result.body.error.code, "BAD_REQUEST");
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

test("GET /api/v1/admin/ops-dashboard/summary returns ops summary", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/ops-dashboard/summary",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.overallStatus, "string");
  assert.equal(typeof result.body.data.alertCount, "number");
  assert.ok(Array.isArray(result.body.data.alerts));
});

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

test("GET /api/v1/admin/observability/traces returns recent traces", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/observability/traces?limit=5&pathContains=/api/v1/admin",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data.items));
});

test("GET /api/v1/admin/observability/dashboard returns observability dashboard", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/observability/dashboard",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.checkedAt, "string");
  assert.equal(typeof result.body.data.process, "object");
  assert.equal(typeof result.body.data.metrics, "object");
  assert.equal(typeof result.body.data.resilience, "object");
});

test("GET /api/v1/resilience/circuit-breakers returns circuit snapshots", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/resilience/circuit-breakers",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data.items));
});

test("GET /api/v1/resilience/circuit-breakers/policies returns circuit policies", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/resilience/circuit-breakers/policies",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data.items));
});

test("GET /api/v1/resilience/circuit-breakers/crawler returns crawler circuit", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/resilience/circuit-breakers/crawler",
    asAdmin(),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.name, "crawler");
  assert.equal(typeof result.body.data.status, "string");
  assert.equal(typeof result.body.data.options, "object");
});

test("GET /api/v1/resilience/circuit-breakers/unknown returns 404", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/resilience/circuit-breakers/unknown",
    asAdmin(),
  );

  assert.equal(result.status, 404);
  assert.equal(result.body.success, false);
  assert.equal(result.body.error.code, "NOT_FOUND");
});

test("POST /api/v1/resilience/circuit-breakers/crawler/reset resets circuit", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/resilience/circuit-breakers/crawler/reset",
    {
      ...asAdmin(),
      method: "POST",
    },
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.name, "crawler");
});
