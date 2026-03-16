import { badRequest, forbidden, notFound } from "../utils/http-error.js";
import { createMediaAssets, deleteMediaAsset, findMediaAsset } from "./media.repository.js";

export async function uploadMedia(userId, payload) {
  const files = Array.isArray(payload?.files) ? payload.files : [];
  if (files.length === 0) throw badRequest("files are required");
  const data = files.map((file) => ({
    uploaderId: userId,
    ownerType: payload?.ownerType ?? "UNKNOWN",
    ownerId: Number(payload?.ownerId ?? 0),
    originalName: file.originalName,
    fileKey: file.fileKey,
    fileUrl: file.fileUrl,
    type: file.type,
    mime: file.mime,
    size: BigInt(file.size ?? 0),
    duration: file.duration ?? null,
    width: file.width ?? null,
    height: file.height ?? null,
  }));
  return createMediaAssets(data);
}

export async function createPresignedUrl(payload) {
  if (!payload?.fileName || !payload?.fileType) throw badRequest("fileName and fileType are required");
  const fileKey = `uploads/${Date.now()}-${payload.fileName}`;
  return {
    uploadUrl: `https://presigned.example.com/${fileKey}`,
    fileKey,
  };
}

export async function getMediaMetadata(id) {
  const item = await findMediaAsset(id);
  if (!item) throw notFound("Media not found");
  return {
    mime: item.mime,
    size: Number(item.size),
    duration: item.duration,
    resolution: item.width && item.height ? `${item.width}x${item.height}` : null,
  };
}

export async function deleteMedia(user, id) {
  const item = await findMediaAsset(id);
  if (!item) throw notFound("Media not found");
  if (item.uploaderId !== user.id && user.role !== "ADMIN") throw forbidden("You cannot delete this media");
  await deleteMediaAsset(id);
  return { message: "Media deleted" };
}

export async function streamMedia(id) {
  const item = await findMediaAsset(id);
  if (!item) throw notFound("Media not found");
  return item;
}
