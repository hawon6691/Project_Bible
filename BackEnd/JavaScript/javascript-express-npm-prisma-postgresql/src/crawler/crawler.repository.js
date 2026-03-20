import { prisma } from "../prisma.js";

export function findCrawlerJobs(filters, page, limit) {
  const where = {
    ...(filters.sellerId ? { sellerId: Number(filters.sellerId) } : {}),
    ...(filters.isActive !== undefined ? { isActive: filters.isActive } : {}),
  };

  return Promise.all([
    prisma.crawlerJob.findMany({
      where,
      orderBy: [{ createdAt: "desc" }, { id: "desc" }],
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.crawlerJob.count({ where }),
  ]);
}

export function findCrawlerJobById(jobId) {
  return prisma.crawlerJob.findUnique({
    where: { id: Number(jobId) },
  });
}

export function createCrawlerJob(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('crawler_jobs', 'id'), COALESCE((SELECT MAX(id) FROM crawler_jobs), 0) + 1, false)",
    );
    return tx.crawlerJob.create({ data });
  });
}

export function updateCrawlerJob(jobId, data) {
  return prisma.crawlerJob.update({
    where: { id: Number(jobId) },
    data: {
      ...data,
      updatedAt: new Date(),
    },
  });
}

export function deleteCrawlerJob(jobId) {
  return prisma.crawlerJob.delete({
    where: { id: Number(jobId) },
  });
}

export function clearCrawlerRunJobReferences(jobId) {
  return prisma.crawlerRun.updateMany({
    where: { jobId: Number(jobId) },
    data: {
      jobId: null,
      updatedAt: new Date(),
    },
  });
}

export function findCrawlerRuns(filters, page, limit) {
  const where = {
    ...(filters.jobId ? { jobId: Number(filters.jobId) } : {}),
    ...(filters.sellerId ? { sellerId: Number(filters.sellerId) } : {}),
    ...(filters.status ? { status: filters.status } : {}),
  };

  return Promise.all([
    prisma.crawlerRun.findMany({
      where,
      orderBy: [{ startedAt: "desc" }, { id: "desc" }],
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.crawlerRun.count({ where }),
  ]);
}

export function countCrawlerRuns(where = {}) {
  return prisma.crawlerRun.count({ where });
}

export function findLatestCrawlerRun(where = {}) {
  return prisma.crawlerRun.findFirst({
    where,
    orderBy: [{ startedAt: "desc" }, { id: "desc" }],
  });
}

export function createCrawlerRun(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('crawler_runs', 'id'), COALESCE((SELECT MAX(id) FROM crawler_runs), 0) + 1, false)",
    );
    return tx.crawlerRun.create({ data });
  });
}
