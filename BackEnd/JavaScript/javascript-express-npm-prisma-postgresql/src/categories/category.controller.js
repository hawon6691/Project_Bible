import {
  createCategory,
  deleteCategory,
  getCategories,
  getCategory,
  updateCategory,
} from "./category.service.js";
import { toCategoryDto } from "./category.mapper.js";
import { success } from "../utils/response.js";

export async function getCategoriesController(_req, res) {
  const { items, meta } = await getCategories();
  res.status(200).json(success(items.map(toCategoryDto), meta));
}

export async function getCategoryController(req, res) {
  const data = await getCategory(req.params.id);
  res.status(200).json(success(toCategoryDto(data)));
}

export async function createCategoryController(req, res) {
  const data = await createCategory(req.body);
  res.status(201).json(success(toCategoryDto(data)));
}

export async function updateCategoryController(req, res) {
  const data = await updateCategory(req.params.id, req.body);
  res.status(200).json(success(toCategoryDto(data)));
}

export async function deleteCategoryController(req, res) {
  const data = await deleteCategory(req.params.id);
  res.status(200).json(success(data));
}
