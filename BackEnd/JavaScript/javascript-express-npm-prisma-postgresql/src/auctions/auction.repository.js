import { prisma } from "../prisma.js";

export function findCategoryById(categoryId) {
  return prisma.category.findUnique({
    where: { id: Number(categoryId) },
    select: {
      id: true,
      name: true,
    },
  });
}

export function findSellerById(sellerId) {
  return prisma.seller.findFirst({
    where: {
      id: Number(sellerId),
      isActive: true,
    },
    select: {
      id: true,
      name: true,
      url: true,
    },
  });
}

export function findAuctions(where, page, limit) {
  return Promise.all([
    prisma.auction.findMany({
      where,
      include: {
        user: {
          select: {
            id: true,
            name: true,
          },
        },
        category: {
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy: [{ createdAt: "desc" }, { id: "desc" }],
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.auction.count({ where }),
  ]);
}

export function findAuctionById(auctionId) {
  return prisma.auction.findUnique({
    where: { id: Number(auctionId) },
    include: {
      user: {
        select: {
          id: true,
          name: true,
        },
      },
      category: {
        select: {
          id: true,
          name: true,
        },
      },
    },
  });
}

export function findAuctionBids(auctionId) {
  return prisma.auctionBid.findMany({
    where: { auctionId: Number(auctionId) },
    include: {
      seller: {
        select: {
          id: true,
          name: true,
          url: true,
        },
      },
    },
    orderBy: [{ price: "asc" }, { id: "asc" }],
  });
}

export function findAuctionBidById(auctionId, bidId) {
  return prisma.auctionBid.findFirst({
    where: {
      id: Number(bidId),
      auctionId: Number(auctionId),
    },
    include: {
      seller: {
        select: {
          id: true,
          name: true,
          url: true,
        },
      },
    },
  });
}

export function createAuction(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('auctions', 'id'), COALESCE((SELECT MAX(id) FROM auctions), 0) + 1, false)",
    );
    return tx.auction.create({
      data,
    });
  });
}

export function createAuctionBid(auctionId, data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('bids', 'id'), COALESCE((SELECT MAX(id) FROM bids), 0) + 1, false)",
    );

    const bid = await tx.auctionBid.create({ data });
    await tx.auction.update({
      where: { id: Number(auctionId) },
      data: {
        bidCount: {
          increment: 1,
        },
      },
    });
    return bid;
  });
}

export function updateAuction(auctionId, data) {
  return prisma.auction.update({
    where: { id: Number(auctionId) },
    data,
  });
}

export function updateAuctionBid(bidId, data) {
  return prisma.auctionBid.update({
    where: { id: Number(bidId) },
    data,
  });
}

export function selectAuctionBid(auctionId, bidId) {
  return prisma.$transaction(async (tx) => {
    await tx.auction.update({
      where: { id: Number(auctionId) },
      data: { status: "CLOSED" },
    });
    return tx.auctionBid.findUnique({
      where: { id: Number(bidId) },
    });
  });
}

export function deleteAuctionBid(auctionId, bidId) {
  return prisma.$transaction(async (tx) => {
    await tx.auctionBid.delete({
      where: { id: Number(bidId) },
    });

    const auction = await tx.auction.findUnique({
      where: { id: Number(auctionId) },
      select: { bidCount: true },
    });

    await tx.auction.update({
      where: { id: Number(auctionId) },
      data: {
        bidCount: Math.max((auction?.bidCount ?? 1) - 1, 0),
      },
    });
  });
}
