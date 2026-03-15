import { createInquiry as createInquiryRecord, findInquiries } from "./inquiry.repository.js";
import { badRequest } from "../utils/http-error.js";

export async function getInquiries(productId) {
  const items = await findInquiries(productId);
  return { items, meta: { total: items.length } };
}

export async function createInquiry(userId, productId, payload) {
  const { title, content, isSecret } = payload ?? {};
  if (!title || !content) {
    throw badRequest("title and content are required");
  }
  return createInquiryRecord({
    productId: Number(productId),
    userId,
    title,
    content,
    isSecret: Boolean(isSecret),
  });
}
