import { getHealth } from "../health/health.service.js";
import { getQueueStats } from "../queue-admin/queue-admin.service.js";
import { getSearchOutboxSummary } from "../search/search.service.js";
import { getCrawlerMonitoring } from "../crawler/crawler.service.js";

function unwrapOrNull(key, result, errors) {
  if (result.status === "fulfilled") {
    return result.value;
  }

  errors[key] = result.reason instanceof Error ? result.reason.message : "Unknown error";
  return null;
}

function readIntEnv(key, fallback, min = Number.MIN_SAFE_INTEGER) {
  const parsed = Number(process.env[key]);
  if (!Number.isFinite(parsed)) {
    return fallback;
  }
  return Math.max(min, Math.trunc(parsed));
}

function buildAlerts({ health, searchSync, crawler, queue, errors }) {
  const alerts = [];

  const searchFailedThreshold = readIntEnv("OPS_ALERT_SEARCH_FAILED_THRESHOLD", 1, 0);
  const crawlerFailedRunsThreshold = readIntEnv("OPS_ALERT_CRAWLER_FAILED_RUNS_THRESHOLD", 1, 0);
  const queueFailedThreshold = readIntEnv("OPS_ALERT_QUEUE_FAILED_THRESHOLD", 1, 0);

  if (health?.status && String(health.status).toUpperCase() !== "UP") {
    alerts.push({
      key: "health",
      severity: "critical",
      message: `Health status is abnormal (${health.status})`,
    });
  }

  if (searchFailedThreshold > 0 && (searchSync?.failed ?? 0) >= searchFailedThreshold) {
    alerts.push({
      key: "searchSync",
      severity: "warning",
      message: `Search sync failures reached the threshold (${searchSync.failed}/${searchFailedThreshold})`,
    });
  }

  if (crawlerFailedRunsThreshold > 0 && (crawler?.failedRunCount ?? 0) >= crawlerFailedRunsThreshold) {
    alerts.push({
      key: "crawler",
      severity: "warning",
      message: `Crawler failed runs reached the threshold (${crawler.failedRunCount}/${crawlerFailedRunsThreshold})`,
    });
  }

  if (queueFailedThreshold > 0) {
    const failedQueue = (queue?.items ?? []).find((item) => (item?.counts?.failed ?? 0) >= queueFailedThreshold);
    if (failedQueue) {
      alerts.push({
        key: "queue",
        severity: "warning",
        message: `Queue failed jobs reached the threshold (${failedQueue.queueName}: ${failedQueue.counts.failed}/${queueFailedThreshold})`,
      });
    }
  }

  if (Object.keys(errors).length > 0) {
    alerts.push({
      key: "partial_failure",
      severity: "critical",
      message: `Partial metric collection failure (${Object.keys(errors).join(", ")})`,
    });
  }

  return alerts;
}

export async function getOpsDashboardSummary() {
  const [healthResult, searchSyncResult, crawlerResult, queueResult] = await Promise.allSettled([
    getHealth(),
    getSearchOutboxSummary(),
    getCrawlerMonitoring(),
    Promise.resolve(getQueueStats()),
  ]);

  const errors = {};
  const health = unwrapOrNull("health", healthResult, errors);
  const searchSync = unwrapOrNull("searchSync", searchSyncResult, errors);
  const crawler = unwrapOrNull("crawler", crawlerResult, errors);
  const queue = unwrapOrNull("queue", queueResult, errors);
  const alerts = buildAlerts({ health, searchSync, crawler, queue, errors });

  return {
    checkedAt: new Date().toISOString(),
    overallStatus: Object.keys(errors).length > 0 ? "degraded" : "up",
    health,
    searchSync,
    crawler,
    queue,
    errors,
    alerts,
    alertCount: alerts.length,
  };
}
