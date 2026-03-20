import { createFaq as createFaqRecord, findFaqs } from "./faq.repository.js";
import { badRequest } from "../utils/http-error.js";

export async function getFaqs(query) {
  const category = query.category ? String(query.category) : undefined;
  const search = String(query.search ?? "").trim();
  const items = await findFaqs(category, search);
  return { items, meta: { total: items.length } };
}

export async function createFaq(payload) {
  const { category, question, answer, sortOrder, isActive } = payload ?? {};
  if (!category || !question || !answer) {
    throw badRequest("category, question, answer are required");
  }
  return createFaqRecord({
    category,
    question,
    answer,
    sortOrder: Number(sortOrder ?? 0),
    isActive: typeof isActive === "boolean" ? isActive : true,
  });
}
