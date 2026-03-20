import { prisma } from "../prisma.js";

export function findFaqs(category, search) {
  return prisma.faq.findMany({
    where: {
      isActive: true,
      ...(category ? { category } : {}),
      ...(search
        ? {
            OR: [
              { question: { contains: search, mode: "insensitive" } },
              { answer: { contains: search, mode: "insensitive" } },
            ],
          }
        : {}),
    },
    orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
  });
}

export function createFaq(data) {
  return prisma.faq.create({ data });
}
