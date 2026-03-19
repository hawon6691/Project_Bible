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

async function runBurst(path, count, init) {
  const results = [];
  for (let index = 0; index < count; index += 1) {
    results.push(await requestJson(context.baseUrl, path, init));
  }
  return results;
}

test("GET /health does not return 429 during a small burst", async () => {
  const results = await runBurst("/health", 12);

  assert.ok(results.every((item) => item.status === 200));
  assert.ok(results.every((item) => item.body.success === true));
});

test("GET /docs/openapi does not return 429 during a small burst", async () => {
  const results = await runBurst("/docs/openapi", 12);

  assert.ok(results.every((item) => item.status === 200));
  assert.ok(results.every((item) => typeof item.body.openapi === "string"));
});

test("GET /api/v1/users/me keeps auth failures stable under repetition", async () => {
  const results = await runBurst("/api/v1/users/me", 8);

  assert.ok(results.every((item) => item.status === 401));
  assert.ok(results.every((item) => item.body.error.code === "UNAUTHORIZED"));
});

test("GET /api/v1/users/me with valid auth remains successful under repetition", async () => {
  const results = await runBurst(
    "/api/v1/users/me",
    8,
    withBearer(context.userToken),
  );

  assert.ok(results.every((item) => item.status === 200));
  assert.ok(results.every((item) => item.body.data.id === context.user.id));
});
