import crypto from "crypto";

import { getCrawlerMonitoring } from "../crawler/crawler.service.js";
import { getOpsDashboardSummary } from "../ops-dashboard/ops-dashboard.service.js";
import { getQueueStats } from "../queue-admin/queue-admin.service.js";
import { getCircuitBreakerPolicies, getCircuitBreakerSnapshots } from "../resilience/resilience.service.js";
import { getSearchOutboxSummary } from "../search/search.service.js";

const traces = [];
const maxTraceBuffer = (() => {
  const parsed = Number(process.env.OBS_TRACE_BUFFER_LIMIT);
  if (!Number.isFinite(parsed)) {
    return 500;
  }
  return Math.max(100, Math.min(5000, Math.trunc(parsed)));
})();

function percentile(sorted, p) {
  if (sorted.length === 0) {
    return 0;
  }
  const index = Math.min(sorted.length - 1, Math.ceil(sorted.length * p) - 1);
  return sorted[index];
}

function unwrapOrFallback(key, result, fallbackErrors) {
  if (result.status === "fulfilled") {
    return result.value;
  }

  fallbackErrors[key] = result.reason instanceof Error ? result.reason.message : "Unknown error";
  return null;
}

export function createRequestId() {
  return `req-${crypto.randomUUID()}`;
}

export function recordHttpTrace(trace) {
  traces.push(trace);
  if (traces.length > maxTraceBuffer) {
    traces.splice(0, traces.length - maxTraceBuffer);
  }
}

export function getRecentTraces(limit = 50, pathContains) {
  const parsedLimit = Number(limit);
  const safeLimit = Number.isInteger(parsedLimit) && parsedLimit > 0 ? Math.min(parsedLimit, 100) : 50;
  const normalized = String(pathContains ?? "").trim().toLowerCase();
  const filtered = normalized
    ? traces.filter((item) => String(item.path).toLowerCase().includes(normalized))
    : traces;

  return filtered.slice(-safeLimit).reverse();
}

export function getMetricsSummary(windowMinutes = 15) {
  const from = Date.now() - windowMinutes * 60_000;
  const windowed = traces.filter((item) => new Date(item.timestamp).getTime() >= from);
  const durations = windowed.map((item) => Number(item.durationMs)).sort((a, b) => a - b);
  const totalRequests = windowed.length;
  const errorRequests = windowed.filter((item) => Number(item.statusCode) >= 400).length;

  return {
    totalRequests,
    errorRequests,
    errorRate: totalRequests === 0 ? 0 : Number((errorRequests / totalRequests).toFixed(4)),
    avgLatencyMs:
      totalRequests === 0
        ? 0
        : Number((durations.reduce((acc, cur) => acc + cur, 0) / totalRequests).toFixed(2)),
    p95LatencyMs: percentile(durations, 0.95),
    p99LatencyMs: percentile(durations, 0.99),
    statusBuckets: {
      s2xx: windowed.filter((item) => item.statusCode >= 200 && item.statusCode < 300).length,
      s3xx: windowed.filter((item) => item.statusCode >= 300 && item.statusCode < 400).length,
      s4xx: windowed.filter((item) => item.statusCode >= 400 && item.statusCode < 500).length,
      s5xx: windowed.filter((item) => item.statusCode >= 500).length,
    },
  };
}

export async function getObservabilityDashboard() {
  const [queueResult, searchSyncResult, crawlerResult, opsSummaryResult] = await Promise.allSettled([
    Promise.resolve(getQueueStats()),
    getSearchOutboxSummary(),
    getCrawlerMonitoring(),
    getOpsDashboardSummary(),
  ]);

  const errors = {};
  const queue = unwrapOrFallback("queue", queueResult, errors);
  const searchSync = unwrapOrFallback("searchSync", searchSyncResult, errors);
  const crawler = unwrapOrFallback("crawler", crawlerResult, errors);
  const opsSummary = unwrapOrFallback("opsSummary", opsSummaryResult, errors);

  return {
    checkedAt: new Date().toISOString(),
    process: {
      uptimeSec: Number(process.uptime().toFixed(2)),
      memory: process.memoryUsage(),
    },
    metrics: getMetricsSummary(),
    queue,
    resilience: {
      circuits: getCircuitBreakerSnapshots(),
      adaptivePolicies: getCircuitBreakerPolicies(),
    },
    searchSync,
    crawler,
    opsSummary,
    ...(Object.keys(errors).length > 0 ? { errors } : {}),
  };
}
