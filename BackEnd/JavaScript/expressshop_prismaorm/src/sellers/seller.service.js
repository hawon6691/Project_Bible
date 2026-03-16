import {
  createSeller as createSellerRecord,
  deleteSeller as deleteSellerRecord,
  findSellerById,
  findSellers,
  updateSeller as updateSellerRecord,
} from "./seller.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getSellers(query) {
  const page = Number(query.page ?? 1);
  const limit = Math.min(Number(query.limit ?? 20), 100);
  const [items, total] = await findSellers(page, limit);
  return {
    items,
    meta: { page, limit, total },
  };
}

export async function getSeller(sellerId) {
  const item = await findSellerById(sellerId);
  if (!item) {
    throw notFound("Seller not found");
  }
  return item;
}

export async function createAdminSeller(payload) {
  const { name, url } = payload ?? {};
  if (!name || !url) {
    throw badRequest("name and url are required");
  }

  return createSellerRecord({
    name,
    url,
    logoUrl: payload?.logoUrl ?? null,
    trustScore: payload?.trustScore !== undefined ? Number(payload.trustScore) : 0,
    trustGrade: payload?.trustGrade ?? null,
    description: payload?.description ?? null,
    isActive: payload?.isActive ?? true,
  });
}

export async function updateAdminSeller(sellerId, payload) {
  const existing = await findSellerById(sellerId);
  if (!existing) {
    throw notFound("Seller not found");
  }

  const data = {};
  if (payload?.name !== undefined) data.name = payload.name;
  if (payload?.url !== undefined) data.url = payload.url;
  if (payload?.logoUrl !== undefined) data.logoUrl = payload.logoUrl;
  if (payload?.trustScore !== undefined) data.trustScore = Number(payload.trustScore);
  if (payload?.trustGrade !== undefined) data.trustGrade = payload.trustGrade;
  if (payload?.description !== undefined) data.description = payload.description;
  if (payload?.isActive !== undefined) data.isActive = Boolean(payload.isActive);

  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  return updateSellerRecord(sellerId, data);
}

export async function deleteAdminSeller(sellerId) {
  const existing = await findSellerById(sellerId);
  if (!existing) {
    throw notFound("Seller not found");
  }

  await deleteSellerRecord(sellerId);
  return { message: "Seller deleted" };
}
