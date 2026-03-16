import {
  countShortformComments,
  countShortformsByUser,
  createShortform,
  createShortformComment,
  createShortformLike,
  deleteShortformById,
  deleteShortformLike,
  findShortformById,
  findShortformComments,
  findShortformLike,
  findShortformRanking,
  findShortforms,
  findShortformsByUser,
  incrementShortformView,
  updateShortformById,
} from "./shortform.repository.js";
import { badRequest, forbidden, notFound } from "../utils/http-error.js";

export async function createShortformItem(userId, payload) {
  if (!payload?.title || !payload?.videoUrl) {
    throw badRequest("title and videoUrl are required");
  }
  const productIds = Array.isArray(payload?.productIds) ? payload.productIds.map(Number).filter(Boolean) : [];
  return createShortform({
    userId,
    title: payload.title,
    videoUrl: payload.videoUrl,
    thumbnailUrl: payload?.thumbnailUrl ?? null,
    durationSec: Number(payload?.durationSec ?? 0),
    transcodeStatus: "PENDING",
    products: productIds.length > 0 ? { create: productIds.map((productId) => ({ productId })) } : undefined,
  });
}

export async function getShortformFeed(query) {
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const items = await findShortforms(limit);
  return { items, meta: { total: items.length, limit } };
}

export async function getShortformDetail(id) {
  const item = await incrementShortformView(id);
  if (!item) throw notFound("Shortform not found");
  return item;
}

export async function toggleShortformLike(userId, shortformId) {
  const item = await findShortformById(shortformId);
  if (!item) throw notFound("Shortform not found");
  const existing = await findShortformLike(shortformId, userId);
  if (existing) {
    const updated = await deleteShortformLike(shortformId, userId);
    return { liked: false, likeCount: updated.likeCount };
  }
  const updated = await createShortformLike(shortformId, userId);
  return { liked: true, likeCount: updated.likeCount };
}

export async function createComment(userId, shortformId, payload) {
  if (!payload?.content) throw badRequest("content is required");
  const item = await findShortformById(shortformId);
  if (!item) throw notFound("Shortform not found");
  return createShortformComment({
    shortformId: Number(shortformId),
    userId,
    content: payload.content,
  });
}

export async function getComments(shortformId, query) {
  const page = Math.max(Number(query?.page ?? 1), 1);
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const [items, total] = await Promise.all([
    findShortformComments(shortformId, page, limit),
    countShortformComments(shortformId),
  ]);
  return { items, meta: { total, page, limit } };
}

export async function getShortformRanking(query) {
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const items = await findShortformRanking(limit);
  return { items, meta: { total: items.length, limit, period: query?.period ?? "day" } };
}

export async function getTranscodeStatus(id) {
  const item = await findShortformById(id);
  if (!item) throw notFound("Shortform not found");
  return {
    status: item.transcodeStatus,
    errorMessage: item.transcodeError,
    transcodedAt: item.transcodedAt,
  };
}

export async function retryTranscode(user, id) {
  const item = await findShortformById(id);
  if (!item) throw notFound("Shortform not found");
  if (item.userId !== user.id && user.role !== "ADMIN") throw forbidden("You cannot retry this shortform");
  await updateShortformById(id, { transcodeStatus: "PENDING", transcodeError: null });
  return { message: "Transcode retry queued", queued: true };
}

export async function deleteShortform(user, id) {
  const item = await findShortformById(id);
  if (!item) throw notFound("Shortform not found");
  if (item.userId !== user.id && user.role !== "ADMIN") throw forbidden("You cannot delete this shortform");
  await deleteShortformById(id);
  return { message: "Shortform deleted" };
}

export async function getShortformsByUser(userId, query) {
  const page = Math.max(Number(query?.page ?? 1), 1);
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const [items, total] = await Promise.all([
    findShortformsByUser(userId, page, limit),
    countShortformsByUser(userId),
  ]);
  return { items, meta: { total, page, limit } };
}
