import fs from "node:fs/promises";

const baseUrl = String(process.env.BASE_URL ?? "").trim().replace(/\/+$/, "");
const apiPrefix = String(process.env.LIVE_SMOKE_PREFIX ?? process.env.API_PREFIX ?? "/api/v1").trim();
const testResultsDir = new URL("../../test-results/", import.meta.url);

if (!baseUrl) {
  console.error("BASE_URL is required");
  process.exit(1);
}

async function check(path) {
  const response = await fetch(`${baseUrl}${path}`);
  const text = await response.text();
  return {
    path,
    status: response.status,
    ok: response.ok,
    hasBody: text.length > 0,
  };
}

const results = await Promise.all([
  check("/health"),
  check(`${apiPrefix}/health`),
  check("/docs/openapi"),
]);

await fs.mkdir(testResultsDir, { recursive: true });
await fs.writeFile(
  new URL("live-smoke.json", testResultsDir),
  JSON.stringify(
    {
      checkedAt: new Date().toISOString(),
      baseUrl,
      apiPrefix,
      results,
    },
    null,
    2,
  ),
  "utf8",
);

if (results.some((item) => !item.ok)) {
  console.error("live-smoke-failed");
  process.exit(1);
}

console.log("live-smoke-ok");
