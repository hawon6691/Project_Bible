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

test("GET /docs/openapi stays publicly accessible", async () => {
  const result = await requestJson(context.baseUrl, "/docs/openapi");

  assert.equal(result.status, 200);
  assert.equal(typeof result.body.openapi, "string");
  assert.equal(typeof result.body.paths, "object");
});

test("GET /docs/swagger stays publicly accessible", async () => {
  const result = await requestText(context.baseUrl, "/docs/swagger");

  assert.equal(result.status, 200);
  assert.match(result.body, /Swagger UI/);
});

test("POST /api/v1/auth/logout rejects missing auth", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/auth/logout", {
    method: "POST",
  });

  assert.equal(result.status, 401);
  assert.equal(result.body.success, false);
  assert.equal(result.body.error.code, "UNAUTHORIZED");
});

test("GET /api/v1/users/me rejects invalid token", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/users/me", {
    headers: {
      Authorization: "Bearer invalid-token",
    },
  });

  assert.equal(result.status, 401);
  assert.equal(result.body.success, false);
  assert.equal(result.body.error.code, "UNAUTHORIZED");
});

test("GET /api/v1/auth/me rejects invalid token", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/auth/me", {
    headers: {
      Authorization: "Bearer invalid-token",
    },
  });

  assert.equal(result.status, 401);
  assert.equal(result.body.success, false);
  assert.equal(result.body.error.code, "UNAUTHORIZED");
});

for (const path of [
  "/api/v1/admin/queues/supported",
  "/api/v1/admin/settings/extensions",
  "/api/v1/admin/observability/metrics",
]) {
  test(`GET ${path} stays forbidden for USER`, async () => {
    const result = await requestJson(
      context.baseUrl,
      path,
      withBearer(context.userToken),
    );

    assert.equal(result.status, 403);
    assert.equal(result.body.success, false);
    assert.equal(result.body.error.code, "FORBIDDEN");
  });
}

test("POST /api/v1/upload/image rejects missing auth before upload handling", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/upload/image", {
    method: "POST",
  });

  assert.equal(result.status, 401);
  assert.equal(result.body.success, false);
  assert.equal(result.body.error.code, "UNAUTHORIZED");
});

test("POST /api/v1/images/upload rejects missing auth before upload handling", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/images/upload", {
    method: "POST",
  });

  assert.equal(result.status, 401);
  assert.equal(result.body.success, false);
  assert.equal(result.body.error.code, "UNAUTHORIZED");
});
