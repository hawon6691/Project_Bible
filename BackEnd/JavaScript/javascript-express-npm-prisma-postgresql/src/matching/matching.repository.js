import { prisma } from "../prisma.js";

export function findPendingMappings(page, limit) {
  const skip = (page - 1) * limit;
  return prisma.productMapping.findMany({
    where: { status: "PENDING" },
    include: {
      seller: { select: { id: true, name: true } },
      product: { select: { id: true, name: true, thumbnailUrl: true } },
      reviewer: { select: { id: true, name: true } },
    },
    orderBy: { createdAt: "desc" },
    skip,
    take: limit,
  });
}

export function countPendingMappings() {
  return prisma.productMapping.count({ where: { status: "PENDING" } });
}

export function findMappingById(id) {
  return prisma.productMapping.findUnique({
    where: { id: Number(id) },
    include: {
      seller: { select: { id: true, name: true } },
      product: { select: { id: true, name: true, thumbnailUrl: true } },
      reviewer: { select: { id: true, name: true } },
    },
  });
}

export function updateMapping(id, data) {
  return prisma.productMapping.update({
    where: { id: Number(id) },
    data,
    include: {
      seller: { select: { id: true, name: true } },
      product: { select: { id: true, name: true, thumbnailUrl: true } },
      reviewer: { select: { id: true, name: true } },
    },
  });
}

export function countMappingStatuses() {
  return Promise.all([
    prisma.productMapping.count({ where: { status: "APPROVED" } }),
    prisma.productMapping.count({ where: { status: "PENDING" } }),
    prisma.productMapping.count({ where: { status: "REJECTED" } }),
  ]);
}

export function autoApproveMappings(adminUserId) {
  return prisma.productMapping.updateMany({
    where: { status: "PENDING", confidence: { gte: 0.9 } },
    data: { status: "APPROVED", reviewedBy: adminUserId, updatedAt: new Date() },
  });
}
