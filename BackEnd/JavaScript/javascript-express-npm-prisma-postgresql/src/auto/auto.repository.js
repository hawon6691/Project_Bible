import { prisma } from "../prisma.js";

export function findCarModels(filters) {
  return prisma.carModel.findMany({
    where: {
      isActive: true,
      ...(filters?.brand ? { brand: filters.brand } : {}),
      ...(filters?.type ? { type: filters.type } : {}),
    },
    orderBy: [{ brand: "asc" }, { name: "asc" }, { id: "asc" }],
  });
}

export function findCarModelById(modelId) {
  return prisma.carModel.findFirst({
    where: {
      id: Number(modelId),
      isActive: true,
    },
  });
}

export function findLeaseOffersByModelId(modelId) {
  return prisma.leaseOffer.findMany({
    where: {
      carModelId: Number(modelId),
      isActive: true,
    },
    orderBy: [{ monthlyPayment: "asc" }, { id: "asc" }],
  });
}
