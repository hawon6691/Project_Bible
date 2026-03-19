import assert from "node:assert/strict";
import fs from "node:fs/promises";

import { appConfig } from "../../src/config/app.js";
import { buildOpenApiSpec } from "../../src/docs/docs.service.js";
import { getRouteCatalog } from "../../src/routes/index.js";

const testResultsDir = new URL("../../test-results/", import.meta.url);

const REQUIRED_PATHS = [
  "/api/v1/errors/codes",
  "/api/v1/query/products",
  "/api/v1/chat/rooms/{id}/messages",
];

const REQUIRED_SOCKET_EVENTS = [
  "joinRoom",
  "leaveRoom",
  "sendMessage",
  "newMessage",
  "messageRead",
  "readReceipt",
  "typing",
  "userTyping",
];

const spec = buildOpenApiSpec(appConfig.apiPrefix);
const routes = getRouteCatalog(appConfig.apiPrefix);

for (const path of REQUIRED_PATHS) {
  assert.ok(spec.paths[path], `Missing OpenAPI path: ${path}`);
}

assert.ok(routes.length >= 100, "Route catalog is unexpectedly small");
assert.ok(Array.isArray(spec["x-socket-events"]), "Socket event catalog must exist");

for (const eventName of REQUIRED_SOCKET_EVENTS) {
  assert.ok(
    spec["x-socket-events"].some((item) => item.name === eventName),
    `Missing socket event description: ${eventName}`,
  );
}

await fs.mkdir(testResultsDir, { recursive: true });
await fs.writeFile(
  new URL("quality-check.json", testResultsDir),
  JSON.stringify(
    {
      checkedAt: new Date().toISOString(),
      routeCount: routes.length,
      requiredPathCount: REQUIRED_PATHS.length,
      socketEventCount: spec["x-socket-events"].length,
    },
    null,
    2,
  ),
  "utf8",
);

console.log(`quality-check-ok:${routes.length}`);
