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
