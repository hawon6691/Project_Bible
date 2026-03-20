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

const adminOnlyEndpoints = [
  {
    path: "/api/v1/admin/observability/metrics",
    assertAdminBody: (body) => {
      assert.equal(typeof body.data.totalRequests, "number");
    },
  },
  {
    path: "/api/v1/admin/settings/extensions",
    assertAdminBody: (body) => {
      assert.ok(Array.isArray(body.data.extensions));
    },
  },
  {
    path: "/api/v1/admin/queues/stats",
    assertAdminBody: (body) => {
      assert.ok(Array.isArray(body.data.items));
      assert.equal(typeof body.data.total, "number");
    },
  },
  {
    path: "/api/v1/admin/ops-dashboard/summary",
    assertAdminBody: (body) => {
      assert.equal(typeof body.data.overallStatus, "string");
    },
  },
  {
    path: "/api/v1/crawler/admin/jobs",
    assertAdminBody: (body) => {
      assert.ok(Array.isArray(body.data));
    },
  },
];

for (const endpoint of adminOnlyEndpoints) {
  test(`GET ${endpoint.path} returns 401 without auth`, async () => {
    const result = await requestJson(context.baseUrl, endpoint.path);

    assert.equal(result.status, 401);
    assert.equal(result.body.success, false);
  });

  test(`GET ${endpoint.path} returns 403 for USER`, async () => {
    const result = await requestJson(
      context.baseUrl,
      endpoint.path,
      withBearer(context.userToken),
    );

    assert.equal(result.status, 403);
    assert.equal(result.body.success, false);
  });

  test(`GET ${endpoint.path} returns 200 for ADMIN`, async () => {
    const result = await requestJson(
      context.baseUrl,
      endpoint.path,
      withBearer(context.adminToken),
    );

    assert.equal(result.status, 200);
    assert.equal(result.body.success, true);
    endpoint.assertAdminBody(result.body);
  });
}
