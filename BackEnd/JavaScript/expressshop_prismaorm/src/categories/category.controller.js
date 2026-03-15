import { getCategories } from "./category.service.js";
import { toCategoryDto } from "./category.mapper.js";
import { success } from "../utils/response.js";

export async function getCategoriesController(_req, res) {
  const { items, meta } = await getCategories();
  res.status(200).json(success(items.map(toCategoryDto), meta));
}
