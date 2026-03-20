import { badRequest, notFound } from "../utils/http-error.js";

const SUPPORTED_QUEUES = [
  "activity-log",
  "video-transcode",
  "crawler-collect",
  "search-index-sync",
];

const queueStore = new Map([
  [
    "activity-log",
    [
      {
        id: "activity-failed-1001",
        name: "activity-log-job",
        data: { userId: 1, action: "LOGIN" },
        status: "failed",
        timestamp: Date.now() - 1000 * 60 * 20,
        processedOn: Date.now() - 1000 * 60 * 19,
        finishedOn: Date.now() - 1000 * 60 * 18,
        attemptsMade: 2,
        failedReason: "Redis timeout",
        stacktrace: ["Error: Redis timeout"],
      },
    ],
  ],
  [
    "video-transcode",
    [
      {
        id: "job-1001-a",
        name: "video-transcode-job",
        data: { mediaId: 11, variant: "1080p" },
        status: "failed",
        timestamp: Date.now() - 1000 * 60 * 12,
        processedOn: Date.now() - 1000 * 60 * 11,
        finishedOn: Date.now() - 1000 * 60 * 10,
        attemptsMade: 3,
        failedReason: "ffmpeg exited with code 1",
        stacktrace: ["Error: ffmpeg exited with code 1"],
      },
      {
        id: "completed-job",
        name: "video-transcode-job",
        data: { mediaId: 12, variant: "720p" },
        status: "completed",
        timestamp: Date.now() - 1000 * 60 * 30,
        processedOn: Date.now() - 1000 * 60 * 29,
        finishedOn: Date.now() - 1000 * 60 * 28,
        attemptsMade: 1,
        failedReason: null,
        stacktrace: [],
      },
    ],
  ],
  [
    "crawler-collect",
    [
      {
        id: "crawler-failed-3001",
        name: "crawler-collect-job",
        data: { sellerId: 1, collectPrice: true },
        status: "failed",
        timestamp: Date.now() - 1000 * 60 * 8,
        processedOn: Date.now() - 1000 * 60 * 7,
        finishedOn: Date.now() - 1000 * 60 * 6,
        attemptsMade: 4,
        failedReason: "Network timeout",
        stacktrace: ["Error: Network timeout"],
      },
    ],
  ],
  [
    "search-index-sync",
    [
      {
        id: "search-waiting-4001",
        name: "search-index-sync-job",
        data: { productId: 1, eventType: "UPDATED" },
        status: "waiting",
        timestamp: Date.now() - 1000 * 60 * 2,
        processedOn: null,
        finishedOn: null,
        attemptsMade: 0,
        failedReason: null,
        stacktrace: [],
      },
    ],
  ],
]);

function resolveQueue(queueName) {
  const key = String(queueName).trim();
  if (!SUPPORTED_QUEUES.includes(key)) {
    throw badRequest(`Unsupported queue (${SUPPORTED_QUEUES.join(", ")})`);
  }

  return queueStore.get(key) ?? [];
}

function toJobSnapshot(job) {
  return {
    id: String(job.id),
    name: job.name,
    data: job.data,
    timestamp: job.timestamp,
    processedOn: job.processedOn ?? null,
    finishedOn: job.finishedOn ?? null,
    attemptsMade: job.attemptsMade ?? 0,
    failedReason: job.failedReason ?? null,
    stacktrace: Array.isArray(job.stacktrace) ? job.stacktrace.slice(-3) : [],
  };
}

function countStatuses(items) {
  return items.reduce(
    (acc, item) => {
      const status = item.status;
      if (status === "waiting") acc.waiting += 1;
      if (status === "active") acc.active += 1;
      if (status === "delayed") acc.delayed += 1;
      if (status === "completed") acc.completed += 1;
      if (status === "failed") acc.failed += 1;
      return acc;
    },
    { waiting: 0, active: 0, delayed: 0, completed: 0, failed: 0 },
  );
}

function normalizePage(value, fallback) {
  const parsed = Number(value ?? fallback);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw badRequest("page must be a positive integer");
  }
  return parsed;
}

function normalizeLimit(value, fallback) {
  const parsed = Number(value ?? fallback);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw badRequest("limit must be a positive integer");
  }
  return Math.min(parsed, 100);
}

function normalizeBooleanFlag(value, fallback) {
  if (value === undefined) return fallback;
  return String(value).toLowerCase() !== "false";
}

function findJob(queueName, jobId) {
  const items = resolveQueue(queueName);
  return items.find((item) => String(item.id) === String(jobId));
}

export function getSupportedQueues() {
  return [...SUPPORTED_QUEUES];
}

export function getQueueStats() {
  const items = SUPPORTED_QUEUES.map((queueName) => {
    const jobs = resolveQueue(queueName);
    return {
      queueName,
      paused: false,
      counts: countStatuses(jobs),
    };
  });

  return {
    total: items.length,
    items,
  };
}

export function getFailedJobs(queueName, query) {
  const page = normalizePage(query.page, 1);
  const limit = normalizeLimit(query.limit, 20);
  const newestFirst = normalizeBooleanFlag(query.newestFirst, true);
  const queue = resolveQueue(queueName);
  const failedJobs = queue.filter((item) => item.status === "failed");
  const ordered = [...failedJobs].sort((a, b) => (newestFirst ? b.timestamp - a.timestamp : a.timestamp - b.timestamp));
  const items = ordered.slice((page - 1) * limit, page * limit).map(toJobSnapshot);

  return {
    items,
    total: failedJobs.length,
    page,
    limit,
  };
}

export function retryFailedJobs(queueName, query) {
  const limit = normalizeLimit(query.limit, 50);
  const queue = resolveQueue(queueName);
  const targets = queue.filter((item) => item.status === "failed").slice(0, limit);

  for (const job of targets) {
    job.status = "waiting";
    job.failedReason = null;
    job.stacktrace = [];
    job.finishedOn = null;
  }

  return {
    queueName: String(queueName),
    requested: targets.length,
    requeuedCount: targets.length,
    jobIds: targets.map((item) => String(item.id)),
  };
}

export function autoRetryFailed(query) {
  const perQueueLimit = normalizeLimit(query.perQueueLimit, 20);
  const maxTotal = normalizeLimit(query.maxTotal, 100);
  let retriedTotal = 0;
  const items = [];

  for (const queueName of SUPPORTED_QUEUES) {
    if (retriedTotal >= maxTotal) break;

    const queue = resolveQueue(queueName);
    const candidates = queue.filter((item) => item.status === "failed").slice(0, perQueueLimit);
    const allowed = candidates.slice(0, maxTotal - retriedTotal);

    for (const job of allowed) {
      job.status = "waiting";
      job.failedReason = null;
      job.stacktrace = [];
      job.finishedOn = null;
    }

    retriedTotal += allowed.length;
    items.push({
      queueName,
      candidateCount: candidates.length,
      retriedCount: allowed.length,
      jobIds: allowed.map((item) => String(item.id)),
    });
  }

  return {
    perQueueLimit,
    maxTotal,
    retriedTotal,
    items,
  };
}

export function retryJob(queueName, jobId) {
  const job = findJob(queueName, jobId);
  if (!job) {
    throw notFound("Job not found");
  }

  if (job.status !== "failed") {
    throw badRequest(`Only failed jobs can be retried (current: ${job.status})`);
  }

  job.status = "waiting";
  job.failedReason = null;
  job.stacktrace = [];
  job.finishedOn = null;

  return {
    queueName: String(queueName),
    jobId: String(job.id),
    retried: true,
  };
}

export function removeJob(queueName, jobId) {
  const key = String(queueName).trim();
  const queue = resolveQueue(key);
  const index = queue.findIndex((item) => String(item.id) === String(jobId));
  if (index < 0) {
    throw notFound("Job not found");
  }

  const [removed] = queue.splice(index, 1);
  return {
    queueName: key,
    jobId: String(removed.id),
    removed: true,
  };
}
