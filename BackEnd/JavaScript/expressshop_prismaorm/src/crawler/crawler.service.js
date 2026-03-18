import { badRequest, notFound } from "../utils/http-error.js";
import { findSellerById } from "../sellers/seller.repository.js";
import { findProductById } from "../products/product.repository.js";
import {
  clearCrawlerRunJobReferences,
  countCrawlerRuns,
  createCrawlerJob,
  createCrawlerRun,
  deleteCrawlerJob,
  findCrawlerJobById,
  findCrawlerJobs,
  findCrawlerRuns,
  findLatestCrawlerRun,
  updateCrawlerJob,
} from "./crawler.repository.js";

function toCrawlerJobDto(item) {
  return {
    id: item.id,
    sellerId: item.sellerId,
    name: item.name,
    cronExpression: item.cronExpression,
    collectPrice: item.collectPrice,
    collectSpec: item.collectSpec,
    detectAnomaly: item.detectAnomaly,
    isActive: item.isActive,
    lastTriggeredAt: item.lastTriggeredAt,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

function toCrawlerRunDto(item) {
  return {
    id: item.id,
    jobId: item.jobId,
    sellerId: item.sellerId,
    productId: item.productId,
    triggerType: item.triggerType,
    collectPrice: item.collectPrice,
    collectSpec: item.collectSpec,
    detectAnomaly: item.detectAnomaly,
    status: item.status,
    startedAt: item.startedAt,
    endedAt: item.endedAt,
    durationMs: item.durationMs,
    collectedPriceCount: item.collectedPriceCount,
    collectedSpecCount: item.collectedSpecCount,
    anomalyCount: item.anomalyCount,
    errorMessage: item.errorMessage,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

async function ensureSeller(sellerId) {
  const seller = await findSellerById(sellerId);
  if (!seller) {
    throw notFound("Seller not found");
  }
  return seller;
}

async function ensureProduct(productId) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }
  return product;
}

function normalizePaging(query) {
  const page = Math.max(Number(query.page ?? 1), 1);
  const limit = Math.min(Math.max(Number(query.limit ?? 20), 1), 100);
  return { page, limit };
}

export async function getCrawlerJobs(query) {
  const { page, limit } = normalizePaging(query);
  const filters = {
    sellerId: query.sellerId,
    isActive:
      query.isActive === undefined
        ? undefined
        : String(query.isActive).toLowerCase() === "true",
  };
  const [items, total] = await findCrawlerJobs(filters, page, limit);
  return {
    items: items.map(toCrawlerJobDto),
    meta: { page, limit, total },
  };
}

export async function createAdminCrawlerJob(payload) {
  const { sellerId, name } = payload ?? {};
  if (!sellerId || !name) {
    throw badRequest("sellerId and name are required");
  }

  await ensureSeller(sellerId);
  const item = await createCrawlerJob({
    sellerId: Number(sellerId),
    name: String(name).trim(),
    cronExpression: payload.cronExpression ? String(payload.cronExpression).trim() : null,
    collectPrice: payload.collectPrice ?? true,
    collectSpec: payload.collectSpec ?? true,
    detectAnomaly: payload.detectAnomaly ?? true,
    isActive: payload.isActive ?? true,
  });

  return toCrawlerJobDto(item);
}

export async function updateAdminCrawlerJob(jobId, payload) {
  const existing = await findCrawlerJobById(jobId);
  if (!existing) {
    throw notFound("Crawler job not found");
  }

  const data = {};
  if (payload?.sellerId !== undefined) {
    await ensureSeller(payload.sellerId);
    data.sellerId = Number(payload.sellerId);
  }
  if (payload?.name !== undefined) data.name = String(payload.name).trim();
  if (payload?.cronExpression !== undefined) data.cronExpression = payload.cronExpression ? String(payload.cronExpression).trim() : null;
  if (payload?.collectPrice !== undefined) data.collectPrice = Boolean(payload.collectPrice);
  if (payload?.collectSpec !== undefined) data.collectSpec = Boolean(payload.collectSpec);
  if (payload?.detectAnomaly !== undefined) data.detectAnomaly = Boolean(payload.detectAnomaly);
  if (payload?.isActive !== undefined) data.isActive = Boolean(payload.isActive);

  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  const item = await updateCrawlerJob(jobId, data);
  return toCrawlerJobDto(item);
}

export async function deleteAdminCrawlerJob(jobId) {
  const existing = await findCrawlerJobById(jobId);
  if (!existing) {
    throw notFound("Crawler job not found");
  }

  await clearCrawlerRunJobReferences(jobId);
  await deleteCrawlerJob(jobId);
  return { message: "Crawler job deleted" };
}

export async function runCrawlerJob(jobId) {
  const job = await findCrawlerJobById(jobId);
  if (!job) {
    throw notFound("Crawler job not found");
  }
  if (!job.isActive) {
    throw badRequest("Inactive crawler job cannot be run");
  }

  const now = new Date();
  const run = await createCrawlerRun({
    jobId: job.id,
    sellerId: job.sellerId,
    productId: null,
    triggerType: "MANUAL",
    collectPrice: job.collectPrice,
    collectSpec: job.collectSpec,
    detectAnomaly: job.detectAnomaly,
    status: "QUEUED",
    startedAt: now,
    endedAt: now,
    durationMs: 0,
    collectedPriceCount: 0,
    collectedSpecCount: 0,
    anomalyCount: 0,
    errorMessage: null,
  });

  await updateCrawlerJob(jobId, { lastTriggeredAt: now });
  return { message: "Crawler job queued", runId: run.id };
}

export async function triggerCrawler(payload) {
  const { sellerId } = payload ?? {};
  if (!sellerId) {
    throw badRequest("sellerId is required");
  }

  await ensureSeller(sellerId);
  if (payload?.productId !== undefined && payload.productId !== null) {
    await ensureProduct(payload.productId);
  }

  const now = new Date();
  const run = await createCrawlerRun({
    jobId: payload.jobId ? Number(payload.jobId) : null,
    sellerId: Number(sellerId),
    productId: payload.productId ? Number(payload.productId) : null,
    triggerType: "MANUAL",
    collectPrice: payload.collectPrice ?? true,
    collectSpec: payload.collectSpec ?? true,
    detectAnomaly: payload.detectAnomaly ?? true,
    status: "QUEUED",
    startedAt: now,
    endedAt: now,
    durationMs: 0,
    collectedPriceCount: 0,
    collectedSpecCount: 0,
    anomalyCount: 0,
    errorMessage: null,
  });

  return { message: "Crawler run queued", runId: run.id };
}

export async function getCrawlerRuns(query) {
  const { page, limit } = normalizePaging(query);
  const [items, total] = await findCrawlerRuns(
    {
      jobId: query.jobId,
      sellerId: query.sellerId,
      status: query.status,
    },
    page,
    limit,
  );

  return {
    items: items.map(toCrawlerRunDto),
    meta: { page, limit, total },
  };
}

export async function getCrawlerMonitoring() {
  const [jobCount, queuedRunCount, processingRunCount, successRunCount, failedRunCount, latestRun, latestSuccess] =
    await Promise.all([
      findCrawlerJobs({}, 1, 1).then(([, total]) => total),
      countCrawlerRuns({ status: "QUEUED" }),
      countCrawlerRuns({ status: "PROCESSING" }),
      countCrawlerRuns({ status: "SUCCESS" }),
      countCrawlerRuns({ status: "FAILED" }),
      findLatestCrawlerRun(),
      findLatestCrawlerRun({ status: "SUCCESS" }),
    ]);

  const finishedCount = successRunCount + failedRunCount;
  const successRate =
    finishedCount === 0 ? 0 : Number(((successRunCount / finishedCount) * 100).toFixed(2));

  return {
    jobCount,
    queuedRunCount,
    processingRunCount,
    successRunCount,
    failedRunCount,
    successRate,
    latestRunAt: latestRun?.startedAt ?? null,
    latestSuccessAt: latestSuccess?.endedAt ?? null,
  };
}
