import { prisma } from "../prisma.js";

function shortformInclude() {
  return {
    user: { select: { id: true, name: true, nickname: true, profileImageUrl: true } },
    products: {
      include: {
        product: { select: { id: true, name: true, thumbnailUrl: true, lowestPrice: true } },
      },
      orderBy: { id: "asc" },
    },
  };
}

export function findShortforms(limit = 20) {
  return prisma.shortform.findMany({
    include: shortformInclude(),
    orderBy: [{ createdAt: "desc" }, { id: "desc" }],
    take: limit,
  });
}

export function findShortformsByUser(userId, page, limit) {
  const skip = (page - 1) * limit;
  return prisma.shortform.findMany({
    where: { userId: Number(userId) },
    include: shortformInclude(),
    orderBy: { createdAt: "desc" },
    skip,
    take: limit,
  });
}

export function countShortformsByUser(userId) {
  return prisma.shortform.count({ where: { userId: Number(userId) } });
}

export function findShortformById(id) {
  return prisma.shortform.findUnique({
    where: { id: Number(id) },
    include: {
      ...shortformInclude(),
      comments: {
        include: { user: { select: { id: true, name: true, nickname: true, profileImageUrl: true } } },
        orderBy: { createdAt: "desc" },
      },
    },
  });
}

export function incrementShortformView(id) {
  return prisma.shortform.update({
    where: { id: Number(id) },
    data: { viewCount: { increment: 1 } },
    include: shortformInclude(),
  });
}

export function createShortform(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('shortforms', 'id'), COALESCE((SELECT MAX(id) FROM shortforms), 0) + 1, false)",
    );
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('shortform_products', 'id'), COALESCE((SELECT MAX(id) FROM shortform_products), 0) + 1, false)",
    );
    return tx.shortform.create({ data, include: shortformInclude() });
  });
}

export function findShortformLike(shortformId, userId) {
  return prisma.shortformLike.findFirst({ where: { shortformId: Number(shortformId), userId } });
}

export function createShortformLike(shortformId, userId) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('shortform_likes', 'id'), COALESCE((SELECT MAX(id) FROM shortform_likes), 0) + 1, false)",
    );
    await tx.shortformLike.create({ data: { shortformId: Number(shortformId), userId } });
    return tx.shortform.update({
      where: { id: Number(shortformId) },
      data: { likeCount: { increment: 1 } },
      select: { id: true, likeCount: true },
    });
  });
}

export function deleteShortformLike(shortformId, userId) {
  return prisma.$transaction(async (tx) => {
    await tx.shortformLike.deleteMany({ where: { shortformId: Number(shortformId), userId } });
    return tx.shortform.update({
      where: { id: Number(shortformId) },
      data: { likeCount: { decrement: 1 } },
      select: { id: true, likeCount: true },
    });
  });
}

export function createShortformComment(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('shortform_comments', 'id'), COALESCE((SELECT MAX(id) FROM shortform_comments), 0) + 1, false)",
    );
    const comment = await tx.shortformComment.create({
      data,
      include: { user: { select: { id: true, name: true, nickname: true, profileImageUrl: true } } },
    });
    await tx.shortform.update({
      where: { id: Number(data.shortformId) },
      data: { commentCount: { increment: 1 } },
    });
    return comment;
  });
}

export function findShortformComments(shortformId, page, limit) {
  const skip = (page - 1) * limit;
  return prisma.shortformComment.findMany({
    where: { shortformId: Number(shortformId) },
    include: { user: { select: { id: true, name: true, nickname: true, profileImageUrl: true } } },
    orderBy: { createdAt: "desc" },
    skip,
    take: limit,
  });
}

export function countShortformComments(shortformId) {
  return prisma.shortformComment.count({ where: { shortformId: Number(shortformId) } });
}

export function findShortformRanking(limit) {
  return prisma.shortform.findMany({
    include: shortformInclude(),
    orderBy: [{ likeCount: "desc" }, { viewCount: "desc" }],
    take: limit,
  });
}

export function updateShortformById(id, data) {
  return prisma.shortform.update({
    where: { id: Number(id) },
    data,
    include: shortformInclude(),
  });
}

export function deleteShortformById(id) {
  return prisma.shortform.delete({ where: { id: Number(id) } });
}
