import { prisma } from "../prisma.js";

const pcBuildPartInclude = {
  product: {
    select: {
      id: true,
      name: true,
      lowestPrice: true,
      price: true,
      discountPrice: true,
    },
  },
  seller: {
    select: {
      id: true,
      name: true,
    },
  },
};

const pcBuildInclude = {
  user: {
    select: {
      id: true,
    },
  },
  pcParts: {
    include: pcBuildPartInclude,
    orderBy: { id: "asc" },
  },
};

export function findMyPcBuilds(userId, page, limit) {
  const where = {
    userId: Number(userId),
    deletedAt: null,
  };

  return Promise.all([
    prisma.pcBuild.findMany({
      where,
      include: pcBuildInclude,
      orderBy: [{ updatedAt: "desc" }, { id: "desc" }],
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.pcBuild.count({ where }),
  ]);
}

export function findPopularPcBuilds(page, limit) {
  const where = { deletedAt: null };

  return Promise.all([
    prisma.pcBuild.findMany({
      where,
      include: pcBuildInclude,
      orderBy: [{ viewCount: "desc" }, { updatedAt: "desc" }, { id: "desc" }],
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.pcBuild.count({ where }),
  ]);
}

export function findPcBuildById(buildId) {
  return prisma.pcBuild.findFirst({
    where: {
      id: Number(buildId),
      deletedAt: null,
    },
    include: pcBuildInclude,
  });
}

export function findOwnedPcBuild(userId, buildId) {
  return prisma.pcBuild.findFirst({
    where: {
      id: Number(buildId),
      userId: Number(userId),
      deletedAt: null,
    },
    include: pcBuildInclude,
  });
}

export function findPcBuildByShareCode(shareCode) {
  return prisma.pcBuild.findFirst({
    where: {
      shareCode,
      deletedAt: null,
    },
    include: pcBuildInclude,
  });
}

export function createPcBuildRecord(data) {
  return prisma.pcBuild.create({
    data,
    include: pcBuildInclude,
  });
}

export function updatePcBuildRecord(buildId, data) {
  return prisma.pcBuild.update({
    where: { id: Number(buildId) },
    data,
    include: pcBuildInclude,
  });
}

export async function deletePcBuildRecord(buildId) {
  await prisma.pcBuildPart.deleteMany({
    where: { buildId: Number(buildId) },
  });

  return prisma.pcBuild.update({
    where: { id: Number(buildId) },
    data: { deletedAt: new Date() },
    include: pcBuildInclude,
  });
}

export function incrementPcBuildViewCount(buildId) {
  return prisma.pcBuild.update({
    where: { id: Number(buildId) },
    data: { viewCount: { increment: 1 } },
  });
}

export function findProductForPcBuild(productId) {
  return prisma.product.findFirst({
    where: {
      id: Number(productId),
      deletedAt: null,
    },
    select: {
      id: true,
      name: true,
      lowestPrice: true,
      price: true,
      discountPrice: true,
      stock: true,
      categoryId: true,
    },
  });
}

export function findPriceEntryForPcBuild(productId, sellerId) {
  return prisma.priceEntry.findFirst({
    where: {
      productId: Number(productId),
      sellerId: Number(sellerId),
      isAvailable: true,
    },
    select: {
      id: true,
      sellerId: true,
      price: true,
      seller: {
        select: {
          id: true,
          name: true,
        },
      },
    },
  });
}

export function findBestPriceEntryForPcBuild(productId) {
  return prisma.priceEntry.findFirst({
    where: {
      productId: Number(productId),
      isAvailable: true,
    },
    orderBy: [{ price: "asc" }, { id: "asc" }],
    select: {
      id: true,
      sellerId: true,
      price: true,
      seller: {
        select: {
          id: true,
          name: true,
        },
      },
    },
  });
}

export function findPcBuildPartByType(buildId, partType) {
  return prisma.pcBuildPart.findFirst({
    where: {
      buildId: Number(buildId),
      partType,
    },
    include: pcBuildPartInclude,
  });
}

export function findPcBuildPartById(buildId, partId) {
  return prisma.pcBuildPart.findFirst({
    where: {
      id: Number(partId),
      buildId: Number(buildId),
    },
    include: pcBuildPartInclude,
  });
}

export function createPcBuildPartRecord(data) {
  return prisma.pcBuildPart.create({
    data,
    include: pcBuildPartInclude,
  });
}

export function updatePcBuildPartRecord(partId, data) {
  return prisma.pcBuildPart.update({
    where: { id: Number(partId) },
    data,
    include: pcBuildPartInclude,
  });
}

export function deletePcBuildPartRecord(partId) {
  return prisma.pcBuildPart.delete({
    where: { id: Number(partId) },
  });
}

export async function syncPcBuildTotalPrice(buildId) {
  const aggregate = await prisma.pcBuildPart.aggregate({
    where: { buildId: Number(buildId) },
    _sum: {
      totalPrice: true,
    },
  });

  return prisma.pcBuild.update({
    where: { id: Number(buildId) },
    data: {
      totalPrice: aggregate._sum.totalPrice ?? 0,
    },
    include: pcBuildInclude,
  });
}

export function listCompatibilityRules() {
  return prisma.pcCompatibilityRule.findMany({
    orderBy: [{ id: "asc" }],
  });
}

export function listEnabledCompatibilityRules() {
  return prisma.pcCompatibilityRule.findMany({
    where: { enabled: true },
    orderBy: [{ id: "asc" }],
  });
}

export function findCompatibilityRuleById(ruleId) {
  return prisma.pcCompatibilityRule.findUnique({
    where: { id: Number(ruleId) },
  });
}

export function createCompatibilityRuleRecord(data) {
  return prisma.pcCompatibilityRule.create({ data });
}

export function updateCompatibilityRuleRecord(ruleId, data) {
  return prisma.pcCompatibilityRule.update({
    where: { id: Number(ruleId) },
    data,
  });
}

export function deleteCompatibilityRuleRecord(ruleId) {
  return prisma.pcCompatibilityRule.delete({
    where: { id: Number(ruleId) },
  });
}
