import { prisma } from "../prisma.js";

const includeVariants = {
  variants: {
    orderBy: [{ id: "asc" }],
  },
};

export async function createImageAssetWithVariants(imageAssetData, variantItems) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('image_assets', 'id'), COALESCE((SELECT MAX(id) FROM image_assets), 0) + 1, false)",
    );
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('image_variants', 'id'), COALESCE((SELECT MAX(id) FROM image_variants), 0) + 1, false)",
    );

    const createdImage = await tx.imageAsset.create({
      data: imageAssetData,
    });

    await tx.imageVariant.createMany({
      data: variantItems.map((item) => ({
        ...item,
        imageId: createdImage.id,
      })),
    });

    await tx.imageAsset.update({
      where: { id: createdImage.id },
      data: {
        processingStatus: "COMPLETED",
      },
    });

    return tx.imageAsset.findUnique({
      where: { id: createdImage.id },
      include: includeVariants,
    });
  });
}

export function findImageAssetById(imageId) {
  return prisma.imageAsset.findUnique({
    where: { id: Number(imageId) },
    include: includeVariants,
  });
}

export function findImageVariants(imageId) {
  return prisma.imageVariant.findMany({
    where: { imageId: Number(imageId) },
    orderBy: [{ id: "asc" }],
  });
}

export async function deleteImageAssetWithVariants(imageId) {
  return prisma.$transaction(async (tx) => {
    await tx.imageVariant.deleteMany({
      where: { imageId: Number(imageId) },
    });

    return tx.imageAsset.delete({
      where: { id: Number(imageId) },
    });
  });
}
