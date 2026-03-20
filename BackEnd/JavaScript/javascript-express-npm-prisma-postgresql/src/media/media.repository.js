import { prisma } from "../prisma.js";

export function createMediaAssets(items) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('media_assets', 'id'), COALESCE((SELECT MAX(id) FROM media_assets), 0) + 1, false)",
    );
    const created = [];
    for (const item of items) {
      created.push(await tx.mediaAsset.create({ data: item }));
    }
    return created;
  });
}

export function findMediaAsset(id) {
  return prisma.mediaAsset.findUnique({ where: { id: Number(id) } });
}

export function deleteMediaAsset(id) {
  return prisma.mediaAsset.delete({ where: { id: Number(id) } });
}
