import {
  findNoticeById,
  findNotices,
  incrementNoticeViewCount,
} from "./notice.repository.js";
import { notFound } from "../utils/http-error.js";

export async function getNotices(query) {
  const page = Number(query.page ?? 1);
  const limit = Math.min(Number(query.limit ?? 20), 100);
  const [items, total] = await findNotices(page, limit);
  return { items, meta: { page, limit, total } };
}

export async function getNotice(noticeId) {
  const item = await findNoticeById(noticeId);
  if (!item) {
    throw notFound("Notice not found");
  }
  return incrementNoticeViewCount(noticeId);
}
