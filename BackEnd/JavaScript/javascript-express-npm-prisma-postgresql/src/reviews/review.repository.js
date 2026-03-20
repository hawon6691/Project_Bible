import { prisma } from "../prisma.js";

export function findReviews(productId) {
  return prisma.review.findMany({
    where: { productId: Number(productId), deletedAt: null },
    include: { user: { select: { id: true, name: true, nickname: true } } },
    orderBy: [{ isBest: "desc" }, { id: "desc" }],
  });
}

export function createReview(data) {
  return prisma.review.create({ data });
}
