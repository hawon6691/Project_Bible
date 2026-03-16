export function toMediaAssetDto(item) {
  return {
    id: item.id,
    uploaderId: item.uploaderId,
    ownerType: item.ownerType,
    ownerId: item.ownerId,
    originalName: item.originalName,
    fileKey: item.fileKey,
    fileUrl: item.fileUrl,
    type: item.type,
    mime: item.mime,
    size: Number(item.size),
    duration: item.duration,
    width: item.width,
    height: item.height,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}
