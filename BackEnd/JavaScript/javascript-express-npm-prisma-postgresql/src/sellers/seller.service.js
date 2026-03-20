import {
  countSellerOrderItems,
  createSeller as createSellerRecord,
  deleteSeller as deleteSellerRecord,
  findSellerReviewById,
  findSellerReviews,
  findSellerById,
  findSellerTrustMetric,
  findUserOrderForSellerReview,
  getSellerReviewAggregates,
  findSellers,
  createSellerReview as createSellerReviewRecord,
  softDeleteSellerReview as softDeleteSellerReviewRecord,
  updateSeller as updateSellerRecord,
  updateSellerReview as updateSellerReviewRecord,
  upsertSellerTrustMetric,
} from "./seller.repository.js";
import { badRequest, conflict, forbidden, notFound } from "../utils/http-error.js";
import { prisma } from "../prisma.js";

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

function toGrade(score) {
  if (score >= 95) return "A+";
  if (score >= 90) return "A";
  if (score >= 80) return "B+";
  if (score >= 70) return "B";
  if (score >= 60) return "C";
  if (score >= 50) return "D";
  return "F";
}

function roundToOneDecimal(value) {
  return Number((Number(value ?? 0)).toFixed(1));
}

function roundToTwoDecimals(value) {
  return Number((Number(value ?? 0)).toFixed(2));
}

function mapSellerReview(item) {
  return {
    id: item.id,
    sellerId: item.sellerId,
    userId: item.userId,
    orderId: item.orderId,
    rating: item.rating,
    deliveryRating: item.deliveryRating,
    content: item.content,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    author: item.user
      ? {
          id: item.user.id,
          name: item.user.name,
          nickname: item.user.nickname,
        }
      : null,
  };
}

function buildTrustDetail(seller, metric) {
  return {
    sellerId: seller.id,
    sellerName: seller.name,
    overallScore: metric.overallScore,
    grade: metric.grade,
    breakdown: {
      deliveryScore: metric.deliveryScore,
      priceAccuracy: metric.priceAccuracy,
      returnRate: roundToTwoDecimals(metric.returnRate),
      responseTime: roundToOneDecimal(metric.responseTimeHours),
      reviewScore: roundToOneDecimal(metric.reviewScore),
      orderCount: metric.orderCount,
      disputeRate: roundToTwoDecimals(metric.disputeRate),
    },
    trend: metric.trend,
    lastUpdatedAt: metric.calculatedAt,
  };
}

async function ensureSeller(sellerId) {
  const seller = await findSellerById(sellerId);
  if (!seller) {
    throw notFound("Seller not found");
  }
  return seller;
}

async function recalculateSellerTrustMetric(sellerId) {
  const seller = await ensureSeller(sellerId);
  const existingMetric = await findSellerTrustMetric(sellerId);
  const aggregates = await getSellerReviewAggregates(sellerId);
  const orderCount = await countSellerOrderItems(sellerId);

  const reviewScore = roundToOneDecimal(aggregates._avg.rating ?? 0);
  const deliveryScore = Math.round((Number(aggregates._avg.deliveryRating ?? 0) / 5) * 100);
  const priceAccuracy = existingMetric?.priceAccuracy ?? 0;
  const returnRate = existingMetric?.returnRate ?? 0;
  const responseTimeHours = existingMetric?.responseTimeHours ?? 0;
  const disputeRate = existingMetric?.disputeRate ?? 0;
  const normalizedReturn = Math.max(0, 100 - Number(returnRate) * 10);
  const normalizedResponse = Math.max(0, 100 - Number(responseTimeHours) * 10);
  const normalizedDispute = Math.max(0, 100 - Number(disputeRate) * 20);
  const normalizedOrders = Math.min(orderCount, 100);
  const overallScore = Math.round(
    deliveryScore * 0.25 +
      priceAccuracy * 0.2 +
      reviewScore * 20 * 0.2 +
      normalizedReturn * 0.1 +
      normalizedResponse * 0.1 +
      normalizedOrders * 0.1 +
      normalizedDispute * 0.05,
  );

  const metric = await upsertSellerTrustMetric(sellerId, {
    deliveryScore,
    priceAccuracy,
    returnRate: roundToTwoDecimals(returnRate),
    responseTimeHours: roundToOneDecimal(responseTimeHours),
    reviewScore: roundToOneDecimal(reviewScore),
    orderCount,
    disputeRate: roundToTwoDecimals(disputeRate),
    overallScore,
    grade: toGrade(overallScore),
    trend: existingMetric?.trend ?? "STABLE",
    calculatedAt: new Date(),
  });

  return buildTrustDetail(seller, metric);
}

export async function getSellerTrust(sellerId) {
  const seller = await ensureSeller(sellerId);
  const metric = await findSellerTrustMetric(sellerId);

  if (metric) {
    return buildTrustDetail(seller, metric);
  }

  return recalculateSellerTrustMetric(sellerId);
}

export async function getSellerReviews(sellerId, query) {
  await ensureSeller(sellerId);
  const page = Number(query.page ?? 1);
  const limit = Math.min(Number(query.limit ?? 20), 100);
  const sort = query.sort === "rating_desc" || query.sort === "rating_asc" ? query.sort : "latest";
  const [items, total] = await findSellerReviews(sellerId, page, limit, sort);
  return {
    items: items.map(mapSellerReview),
    meta: { page, limit, total },
  };
}

export async function createSellerReview(userId, sellerId, payload) {
  const { orderId, rating, deliveryRating, content } = payload ?? {};
  if (!orderId || !rating || !deliveryRating || !content) {
    throw badRequest("orderId, rating, deliveryRating, content are required");
  }

  await ensureSeller(sellerId);

  const matchedOrder = await findUserOrderForSellerReview(userId, orderId, sellerId);
  if (!matchedOrder) {
    throw badRequest("Order does not belong to user or seller is not included");
  }

  const existing = await prisma.sellerReview.findFirst({
    where: {
      userId: Number(userId),
      orderId: Number(orderId),
    },
  });
  if (existing) {
    throw conflict("Seller review already exists for this order");
  }

  const item = await createSellerReviewRecord({
    sellerId: Number(sellerId),
    userId: Number(userId),
    orderId: Number(orderId),
    rating: Number(rating),
    deliveryRating: Number(deliveryRating),
    content: String(content).trim(),
  });

  await recalculateSellerTrustMetric(sellerId);
  return mapSellerReview(item);
}

export async function updateSellerReview(actor, reviewId, payload) {
  const review = await findSellerReviewById(reviewId);
  if (!review || review.deletedAt) {
    throw notFound("Seller review not found");
  }

  if (review.userId !== Number(actor.id)) {
    throw forbidden();
  }

  const data = {};
  if (payload?.rating !== undefined) data.rating = Number(payload.rating);
  if (payload?.deliveryRating !== undefined) data.deliveryRating = Number(payload.deliveryRating);
  if (payload?.content !== undefined) data.content = String(payload.content).trim();
  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  const item = await updateSellerReviewRecord(reviewId, data);
  await recalculateSellerTrustMetric(review.sellerId);
  return mapSellerReview(item);
}

export async function deleteSellerReview(actor, reviewId) {
  const review = await findSellerReviewById(reviewId);
  if (!review || review.deletedAt) {
    throw notFound("Seller review not found");
  }

  if (review.userId !== Number(actor.id) && actor.role !== "ADMIN") {
    throw forbidden();
  }

  await softDeleteSellerReviewRecord(reviewId);
  await recalculateSellerTrustMetric(review.sellerId);
  return { message: "Seller review deleted" };
}
