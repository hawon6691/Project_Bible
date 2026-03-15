import { findCategories } from "./category.repository.js";

export async function getCategories() {
  const items = await findCategories();
  return { items, meta: { total: items.length } };
}
