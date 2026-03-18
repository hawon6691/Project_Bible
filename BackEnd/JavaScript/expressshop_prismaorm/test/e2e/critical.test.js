import assert from "node:assert/strict";
import { once } from "node:events";
import test, { after, before } from "node:test";

import { signAccessToken } from "../../src/auth/token.service.js";
import { createApp } from "../../src/app.js";
import { prisma } from "../../src/prisma.js";

let server;
let baseUrl;
let adminToken;
let userToken;

async function requestJson(path, init = {}) {
  const response = await fetch(`${baseUrl}${path}`, init);
  const text = await response.text();
  return {
    status: response.status,
    body: text ? JSON.parse(text) : null,
  };
}

before(async () => {
  const [admin, user] = await Promise.all([
    prisma.user.findFirst({
      where: { role: "ADMIN", deletedAt: null },
      select: { id: true, email: true, role: true },
    }),
    prisma.user.findFirst({
      where: { role: "USER", deletedAt: null },
      select: { id: true, email: true, role: true },
    }),
  ]);

  assert.ok(admin, "Seed admin account is required");
  assert.ok(user, "Seed user account is required");

  adminToken = signAccessToken(admin);
  userToken = signAccessToken(user);

  server = createApp().listen(0, "127.0.0.1");
  await once(server, "listening");
  const address = server.address();
  baseUrl = `http://127.0.0.1:${address.port}`;
});

after(async () => {
  if (server) {
    await new Promise((resolve) => server.close(resolve));
  }
  await prisma.$disconnect();
});

test("GET /health returns UP", async () => {
  const result = await requestJson("/health");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.status, "UP");
});

test("GET /api/v1/docs-status returns pending docs wiring state", async () => {
  const result = await requestJson("/api/v1/docs-status");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.swagger, "pending");
  assert.equal(result.body.data.openapi, "pending");
});

test("GET /api/v1/products returns public product list", async () => {
  const result = await requestJson("/api/v1/products");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
});

test("GET /api/v1/users/me requires auth", async () => {
  const result = await requestJson("/api/v1/users/me");

  assert.equal(result.status, 401);
  assert.equal(result.body.success, false);
});

test("GET /api/v1/users/me returns current user with bearer token", async () => {
  const result = await requestJson("/api/v1/users/me", {
    headers: {
      Authorization: `Bearer ${userToken}`,
    },
  });

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.role, "USER");
});

test("GET /api/v1/admin/observability/metrics rejects non-admin user", async () => {
  const result = await requestJson("/api/v1/admin/observability/metrics", {
    headers: {
      Authorization: `Bearer ${userToken}`,
    },
  });

  assert.equal(result.status, 403);
  assert.equal(result.body.success, false);
});

test("GET /api/v1/admin/observability/metrics allows admin user", async () => {
  const result = await requestJson("/api/v1/admin/observability/metrics", {
    headers: {
      Authorization: `Bearer ${adminToken}`,
    },
  });

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.totalRequests, "number");
});

test("GET /api/v1/pc-builds returns current user build list", async () => {
  const result = await requestJson("/api/v1/pc-builds?page=1&limit=5", {
    headers: {
      Authorization: `Bearer ${userToken}`,
    },
  });

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
  assert.equal(typeof result.body.meta.total, "number");
});

test("GET /api/v1/images/:id/variants returns 404 for missing image", async () => {
  const result = await requestJson("/api/v1/images/999999/variants");

  assert.equal(result.status, 404);
  assert.equal(result.body.success, false);
});
