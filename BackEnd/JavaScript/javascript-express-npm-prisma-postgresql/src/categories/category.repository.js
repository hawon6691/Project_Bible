import { prisma } from "../prisma.js";

export function findCategories() {
  return prisma.category.findMany({
    select: {
      id: true,
      name: true,
      parentId: true,
      sortOrder: true,
      createdAt: true,
      updatedAt: true,
    },
    orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
  });
}

export function findCategoryById(categoryId) {
  return prisma.category.findUnique({
    where: { id: Number(categoryId) },
    select: {
      id: true,
      name: true,
      parentId: true,
      sortOrder: true,
      createdAt: true,
      updatedAt: true,
    },
  });
}

export async function existsCategoryChildren(categoryId) {
  const count = await prisma.category.count({
    where: { parentId: Number(categoryId) },
  });
  return count > 0;
}

export async function existsCategoryProducts(categoryId) {
  const count = await prisma.product.count({
    where: { categoryId: Number(categoryId) },
  });
  return count > 0;
}

export function createCategory(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('categories', 'id'), COALESCE((SELECT MAX(id) FROM categories), 0) + 1, false)",
    );
    return tx.category.create({
      data,
    });
  });
}

export function updateCategory(categoryId, data) {
  return prisma.category.update({
    where: { id: Number(categoryId) },
    data,
  });
}

export function deleteCategory(categoryId) {
  return prisma.category.delete({
    where: { id: Number(categoryId) },
  });
}
