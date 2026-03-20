import {
  createCategory as createCategoryRecord,
  deleteCategory as deleteCategoryRecord,
  existsCategoryChildren,
  existsCategoryProducts,
  findCategories,
  findCategoryById,
  updateCategory as updateCategoryRecord,
} from "./category.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getCategories() {
  const items = await findCategories();
  return { items, meta: { total: items.length } };
}

export async function getCategory(categoryId) {
  const item = await findCategoryById(categoryId);
  if (!item) {
    throw notFound("Category not found");
  }
  return item;
}

export async function createCategory(payload) {
  const { name, parentId, sortOrder } = payload ?? {};
  if (!name) {
    throw badRequest("name is required");
  }

  return createCategoryRecord({
    name,
    parentId: parentId ? Number(parentId) : null,
    sortOrder: sortOrder !== undefined ? Number(sortOrder) : 0,
  });
}

export async function updateCategory(categoryId, payload) {
  const existing = await findCategoryById(categoryId);
  if (!existing) {
    throw notFound("Category not found");
  }

  const data = {};
  if (payload?.name !== undefined) data.name = payload.name;
  if (payload?.parentId !== undefined) data.parentId = payload.parentId ? Number(payload.parentId) : null;
  if (payload?.sortOrder !== undefined) data.sortOrder = Number(payload.sortOrder);

  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  return updateCategoryRecord(categoryId, data);
}

export async function deleteCategory(categoryId) {
  const existing = await findCategoryById(categoryId);
  if (!existing) {
    throw notFound("Category not found");
  }

  if (await existsCategoryChildren(categoryId)) {
    throw badRequest("Child categories exist");
  }

  if (await existsCategoryProducts(categoryId)) {
    throw badRequest("Products are linked to this category");
  }

  try {
    await deleteCategoryRecord(categoryId);
  } catch (error) {
    if (error?.code === "P2003") {
      throw badRequest("Category has related records");
    }
    throw error;
  }

  return { message: "Category deleted" };
}
