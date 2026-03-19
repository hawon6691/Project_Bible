import fs from "node:fs/promises";

import { bootPerfApp, measurePath, shutdownPerfApp } from "./_support/perf-harness.js";

const testResultsDir = new URL("../../test-results/", import.meta.url);
const checks = [
  { path: "/api/v1/search?q=pro&limit=10", iterations: 10, avgThresholdMs: 700, maxThresholdMs: 1800 },
  { path: "/api/v1/search/autocomplete?q=pro&limit=5", iterations: 10, avgThresholdMs: 500, maxThresholdMs: 1200 },
  { path: "/api/v1/rankings/products/popular?limit=5", iterations: 10, avgThresholdMs: 500, maxThresholdMs: 1200 },
];

function summarize(path, samples) {
  const durations = samples.map((item) => item.durationMs);
  return {
    path,
    iterations: samples.length,
    avgMs: Number((durations.reduce((sum, value) => sum + value, 0) / durations.length).toFixed(2)),
    maxMs: Math.max(...durations),
    statuses: [...new Set(samples.map((item) => item.status))],
  };
}

const context = await bootPerfApp();

try {
  const summaries = [];

  for (const check of checks) {
    const samples = await measurePath(context.baseUrl, check.path, check.iterations);
    const summary = summarize(check.path, samples);
    summaries.push(summary);

    if (summary.statuses.some((status) => status >= 400)) {
      throw new Error(`Unexpected status in search perf test for ${check.path}`);
    }
    if (summary.avgMs > check.avgThresholdMs || summary.maxMs > check.maxThresholdMs) {
      throw new Error(`Perf threshold exceeded for ${check.path}`);
    }
  }

  await fs.mkdir(testResultsDir, { recursive: true });
  await fs.writeFile(
    new URL("search-ranking-summary.json", testResultsDir),
    JSON.stringify(
      {
        checkedAt: new Date().toISOString(),
        summaries,
      },
      null,
      2,
    ),
    "utf8",
  );

  console.log("search-ranking-perf-ok");
} finally {
  await shutdownPerfApp(context);
}
