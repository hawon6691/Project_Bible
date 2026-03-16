import {
  createDeal,
  deleteDealById,
  findActiveDeals,
  findDealById,
  updateDeal,
} from "./deal.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

function buildDealProductCreates(payload) {
  if (!Array.isArray(payload?.products)) {
    return [];
  }

  return payload.products
    .map((item) => ({
      productId: Number(item.productId),
      dealPrice: Number(item.dealPrice),
      stock: Number(item.stock),
    }))
    .filter((item) => item.productId && item.dealPrice && item.stock >= 0);
}

export async function getDeals() {
  const items = await findActiveDeals(new Date());
  return { items, meta: { total: items.length } };
}

export async function getDeal(id) {
  const item = await findDealById(id);
  if (!item) throw notFound("Deal not found");
  return item;
}

export async function createAdminDeal(payload) {
  const productId = Number(payload?.productId ?? payload?.products?.[0]?.productId);
  if (!productId || !payload?.title || !payload?.startDate || !payload?.endDate) {
    throw badRequest("productId, title, startDate, endDate are required");
  }

  const dealProducts = buildDealProductCreates(payload);

  return createDeal({
    productId,
    title: payload.title,
    description: payload?.description ?? null,
    discountRate: Number(payload?.discountRate ?? 0),
    startAt: new Date(payload.startDate),
    endAt: new Date(payload.endDate),
    isActive: payload?.isActive ?? true,
    dealProducts: dealProducts.length > 0 ? { create: dealProducts } : undefined,
  });
}

export async function updateAdminDeal(id, payload) {
  const existing = await findDealById(id);
  if (!existing) throw notFound("Deal not found");

  return updateDeal(id, {
    title: payload?.title ?? undefined,
    description: payload?.description ?? undefined,
    discountRate: payload?.discountRate !== undefined ? Number(payload.discountRate) : undefined,
    startAt: payload?.startDate ? new Date(payload.startDate) : undefined,
    endAt: payload?.endDate ? new Date(payload.endDate) : undefined,
    isActive: payload?.isActive !== undefined ? Boolean(payload.isActive) : undefined,
  });
}

export async function deleteAdminDeal(id) {
  const existing = await findDealById(id);
  if (!existing) throw notFound("Deal not found");
  await deleteDealById(id);
  return { message: "Deal deleted" };
}
