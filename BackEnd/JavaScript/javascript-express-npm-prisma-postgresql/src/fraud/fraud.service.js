import {
  findFraudAlertById,
  findFraudAlerts,
  findProductById,
  findRealPriceEntries,
  updateFraudAlert,
  updatePriceEntryAvailability,
} from "./fraud.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

const FRAUD_STATUSES = new Set(["PENDING", "APPROVED", "REJECTED"]);

function calculateTotalPrice(entry) {
  if (entry.shippingType === "FREE") {
    return entry.price;
  }
  return entry.price + (entry.shippingFee ?? 0);
}

export async function getRealPrice(productId, query) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }

  const sellerId = query?.sellerId ? Number(query.sellerId) : null;
  if (query?.sellerId !== undefined && Number.isNaN(sellerId)) {
    throw badRequest("sellerId must be a number");
  }

  const entries = await findRealPriceEntries(productId, sellerId);
  if (entries.length === 0) {
    throw notFound("Price entry not found");
  }

  const selected = [...entries].sort((a, b) => {
    const totalGap = calculateTotalPrice(a) - calculateTotalPrice(b);
    if (totalGap !== 0) return totalGap;
    return a.id - b.id;
  })[0];

  return {
    productId: Number(productId),
    sellerId: selected.sellerId,
    sellerName: selected.seller?.name ?? null,
    productPrice: selected.price,
    shippingFee: selected.shippingFee,
    totalPrice: calculateTotalPrice(selected),
    shippingType: selected.shippingType,
  };
}

export async function getFraudAlerts(query) {
  const page = Math.max(Number(query?.page ?? 1), 1);
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const status = query?.status ? String(query.status).toUpperCase() : null;

  if (status && !FRAUD_STATUSES.has(status)) {
    throw badRequest("status must be one of PENDING, APPROVED, REJECTED");
  }

  const [items, total] = await findFraudAlerts(status, page, limit);
  return {
    items: items.map((item) => ({
      id: item.id,
      productId: item.productId,
      productName: item.product?.name ?? null,
      sellerId: item.sellerId,
      sellerName: item.seller?.name ?? null,
      priceEntryId: item.priceEntryId,
      detectedPrice: item.detectedPrice,
      averagePrice: item.averagePrice,
      deviationPercent: Number(item.deviationPercent),
      status: item.status,
      reviewedBy: item.reviewedBy,
      isAvailable: item.priceEntry?.isAvailable ?? null,
      createdAt: item.createdAt,
    })),
    meta: {
      total,
      page,
      limit,
    },
  };
}

export async function approveFraudAlert(alertId, adminUserId) {
  const alert = await findFraudAlertById(alertId);
  if (!alert) {
    throw notFound("Fraud alert not found");
  }

  await updateFraudAlert(alertId, {
    status: "APPROVED",
    reviewedBy: Number(adminUserId),
  });

  return { message: "Fraud alert approved" };
}

export async function rejectFraudAlert(alertId, adminUserId) {
  const alert = await findFraudAlertById(alertId);
  if (!alert) {
    throw notFound("Fraud alert not found");
  }

  await updateFraudAlert(alertId, {
    status: "REJECTED",
    reviewedBy: Number(adminUserId),
  });
  await updatePriceEntryAvailability(alert.priceEntryId, false);

  return { message: "Fraud alert rejected" };
}
