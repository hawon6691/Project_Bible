import {
  createAuction,
  createAuctionBid,
  deleteAuctionBid,
  findAuctionBidById,
  findAuctionBids,
  findAuctionById,
  findAuctions,
  findCategoryById,
  findSellerById,
  selectAuctionBid,
  updateAuction,
  updateAuctionBid,
} from "./auction.repository.js";
import { badRequest, forbidden, notFound } from "../utils/http-error.js";

const AUCTION_STATUSES = new Set(["OPEN", "CLOSED", "CANCELLED", "COMPLETED"]);

function isAuctionOpen(auction) {
  return auction.status === "OPEN" && auction.expiresAt > new Date();
}

function toAuctionSummary(auction) {
  return {
    id: auction.id,
    userId: auction.userId,
    userName: auction.user?.name ?? null,
    categoryId: auction.categoryId,
    categoryName: auction.category?.name ?? null,
    title: auction.title,
    budget: auction.budget,
    status: auction.status,
    bidCount: auction.bidCount,
    expiresAt: auction.expiresAt,
    createdAt: auction.createdAt,
    updatedAt: auction.updatedAt,
  };
}

function toBidDto(bid) {
  return {
    id: bid.id,
    auctionId: bid.auctionId,
    sellerId: bid.sellerId,
    sellerName: bid.seller?.name ?? null,
    price: bid.price,
    description: bid.description,
    deliveryDays: bid.deliveryDays,
    createdAt: bid.createdAt,
    updatedAt: bid.updatedAt,
  };
}

async function requireAuction(auctionId) {
  const auction = await findAuctionById(auctionId);
  if (!auction) {
    throw notFound("Auction not found");
  }
  return auction;
}

async function requireBid(auctionId, bidId) {
  const bid = await findAuctionBidById(auctionId, bidId);
  if (!bid) {
    throw notFound("Bid not found");
  }
  return bid;
}

async function resolveSellerFromUser(userId) {
  const seller = await findSellerById(userId);
  if (!seller) {
    throw badRequest("Seller profile not found");
  }
  return seller;
}

export async function getAuctions(query) {
  const page = Math.max(Number(query?.page ?? 1), 1);
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const status = query?.status ? String(query.status).toUpperCase() : null;
  const categoryId = query?.categoryId ? Number(query.categoryId) : null;

  if (status && !AUCTION_STATUSES.has(status)) {
    throw badRequest("status must be one of OPEN, CLOSED, CANCELLED, COMPLETED");
  }
  if (query?.categoryId !== undefined && Number.isNaN(categoryId)) {
    throw badRequest("categoryId must be a number");
  }

  const where = {
    ...(status ? { status } : {}),
    ...(categoryId ? { categoryId } : {}),
  };

  const [items, total] = await findAuctions(where, page, limit);
  return {
    items: items.map(toAuctionSummary),
    meta: {
      total,
      page,
      limit,
    },
  };
}

export async function getAuction(auctionId) {
  const auction = await requireAuction(auctionId);
  const bids = await findAuctionBids(auctionId);

  return {
    ...toAuctionSummary(auction),
    description: auction.description,
    specs: {},
    bids: bids.map(toBidDto),
  };
}

export async function createAuctionRequest(userId, payload) {
  if (!payload?.title) throw badRequest("title is required");
  if (!payload?.description) throw badRequest("description is required");
  if (!payload?.categoryId) throw badRequest("categoryId is required");

  const categoryId = Number(payload.categoryId);
  if (Number.isNaN(categoryId)) {
    throw badRequest("categoryId must be a number");
  }

  const category = await findCategoryById(categoryId);
  if (!category) {
    throw notFound("Category not found");
  }

  const created = await createAuction({
    userId: Number(userId),
    title: payload.title,
    description: payload.description,
    categoryId,
    budget: payload?.budget !== undefined && payload?.budget !== null ? Number(payload.budget) : null,
    status: "OPEN",
    bidCount: 0,
    expiresAt: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000),
  });

  return getAuction(created.id);
}

export async function createBid(userId, auctionId, payload) {
  if (!payload?.price) throw badRequest("price is required");
  if (!payload?.deliveryDays) throw badRequest("deliveryDays is required");

  const price = Number(payload.price);
  const deliveryDays = Number(payload.deliveryDays);
  if (Number.isNaN(price) || Number.isNaN(deliveryDays)) {
    throw badRequest("price and deliveryDays must be numbers");
  }

  const seller = await resolveSellerFromUser(userId);
  const auction = await requireAuction(auctionId);
  if (!isAuctionOpen(auction)) {
    throw badRequest("Auction is not open");
  }

  const bid = await createAuctionBid(auctionId, {
    auctionId: Number(auctionId),
    sellerId: seller.id,
    price,
    description: payload?.description ?? null,
    deliveryDays,
  });

  return toBidDto({
    ...bid,
    seller,
  });
}

export async function selectBid(userId, auctionId, bidId) {
  const auction = await requireAuction(auctionId);
  if (auction.userId !== Number(userId)) {
    throw forbidden("Only the auction owner can select a bid");
  }
  if (!isAuctionOpen(auction)) {
    throw badRequest("Auction is not open");
  }

  const bid = await requireBid(auctionId, bidId);
  await selectAuctionBid(auctionId, bid.id);
  return { message: "낙찰을 선택했습니다." };
}

export async function cancelAuction(userId, auctionId) {
  const auction = await requireAuction(auctionId);
  if (auction.userId !== Number(userId)) {
    throw forbidden("Only the auction owner can cancel the auction");
  }
  if (!isAuctionOpen(auction)) {
    throw badRequest("Auction is not open");
  }

  await updateAuction(auctionId, { status: "CANCELLED" });
  return { message: "역경매가 취소되었습니다." };
}

export async function updateBid(userId, auctionId, bidId, payload) {
  const seller = await resolveSellerFromUser(userId);
  const auction = await requireAuction(auctionId);
  if (!isAuctionOpen(auction)) {
    throw badRequest("Auction is not open");
  }

  const bid = await requireBid(auctionId, bidId);
  if (bid.sellerId !== seller.id) {
    throw forbidden("Only the bid owner can update this bid");
  }

  const data = {};
  if (payload?.price !== undefined) data.price = Number(payload.price);
  if (payload?.description !== undefined) data.description = payload.description;
  if (payload?.deliveryDays !== undefined) data.deliveryDays = Number(payload.deliveryDays);

  if (Object.keys(data).length === 0) {
    throw badRequest("At least one field is required");
  }

  const updated = await updateAuctionBid(bid.id, data);
  return toBidDto({
    ...updated,
    seller,
  });
}

export async function deleteBid(userId, auctionId, bidId) {
  const seller = await resolveSellerFromUser(userId);
  const auction = await requireAuction(auctionId);
  if (!isAuctionOpen(auction)) {
    throw badRequest("Auction is not open");
  }

  const bid = await requireBid(auctionId, bidId);
  if (bid.sellerId !== seller.id) {
    throw forbidden("Only the bid owner can delete this bid");
  }

  await deleteAuctionBid(auctionId, bid.id);
  return { message: "입찰이 삭제되었습니다." };
}
