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

test("GET /api/v1/auth/me returns current authenticated user", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/auth/me",
    withBearer(context.userToken),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.id, context.user.id);
});

test("GET /api/v1/search returns public search result payload", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/search?q=pro&limit=5",
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data.hits));
  assert.equal(typeof result.body.data.totalCount, "number");
  assert.equal(typeof result.body.data.facets, "object");
});

test("GET /api/v1/search/autocomplete returns keyword and product suggestions", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/search/autocomplete?q=pro&limit=5",
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data.keywords));
  assert.ok(Array.isArray(result.body.data.products));
  assert.ok(Array.isArray(result.body.data.categories));
});

test("POST /api/v1/search/recent persists recent search keyword for authenticated user", async () => {
  const saveResult = await requestJson(
    context.baseUrl,
    "/api/v1/search/recent",
    {
      method: "POST",
      headers: {
        ...withBearer(context.userToken).headers,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        keyword: "codex-search-keyword",
      }),
    },
  );

  assert.equal(saveResult.status, 201);
  assert.equal(saveResult.body.success, true);

  const recentResult = await requestJson(
    context.baseUrl,
    "/api/v1/search/recent",
    withBearer(context.userToken),
  );

  assert.equal(recentResult.status, 200);
  assert.equal(recentResult.body.success, true);
  assert.ok(Array.isArray(recentResult.body.data));
  assert.ok(recentResult.body.data.some((item) => item.keyword === "codex-search-keyword"));
});
