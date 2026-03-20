import { prisma } from "../prisma.js";

export function findInquiries(productId) {
  return prisma.inquiry.findMany({
    where: { productId: Number(productId) },
    include: {
      user: { select: { id: true, name: true, nickname: true } },
      answeredUser: { select: { id: true, name: true } },
    },
    orderBy: { id: "desc" },
  });
}

export function createInquiry(data) {
  return prisma.inquiry.create({ data });
}
