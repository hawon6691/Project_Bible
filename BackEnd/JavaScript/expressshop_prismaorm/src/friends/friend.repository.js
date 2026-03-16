import { prisma } from "../prisma.js";

function userSelect() {
  return {
    id: true,
    email: true,
    name: true,
    nickname: true,
    profileImageUrl: true,
    role: true,
  };
}

export function createFriendship(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('friendships', 'id'), COALESCE((SELECT MAX(id) FROM friendships), 0) + 1, false)",
    );
    return tx.friendship.create({
      data,
      include: { requester: { select: userSelect() }, addressee: { select: userSelect() } },
    });
  });
}

export function findFriendshipPair(userId, targetUserId) {
  return prisma.friendship.findFirst({
    where: {
      OR: [
        { requesterId: userId, addresseeId: Number(targetUserId) },
        { requesterId: Number(targetUserId), addresseeId: userId },
      ],
    },
  });
}

export function findFriendshipById(id) {
  return prisma.friendship.findUnique({
    where: { id: Number(id) },
    include: { requester: { select: userSelect() }, addressee: { select: userSelect() } },
  });
}

export function updateFriendship(id, data) {
  return prisma.friendship.update({
    where: { id: Number(id) },
    data,
    include: { requester: { select: userSelect() }, addressee: { select: userSelect() } },
  });
}

export function deleteFriendshipPair(userId, targetUserId) {
  return prisma.friendship.deleteMany({
    where: {
      status: "ACCEPTED",
      OR: [
        { requesterId: userId, addresseeId: Number(targetUserId) },
        { requesterId: Number(targetUserId), addresseeId: userId },
      ],
    },
  });
}

export function findAcceptedFriends(userId, page, limit) {
  const skip = (page - 1) * limit;
  return prisma.friendship.findMany({
    where: {
      status: "ACCEPTED",
      OR: [{ requesterId: userId }, { addresseeId: userId }],
    },
    include: { requester: { select: userSelect() }, addressee: { select: userSelect() } },
    orderBy: { updatedAt: "desc" },
    skip,
    take: limit,
  });
}

export function countAcceptedFriends(userId) {
  return prisma.friendship.count({
    where: { status: "ACCEPTED", OR: [{ requesterId: userId }, { addresseeId: userId }] },
  });
}

export function findReceivedRequests(userId, page, limit) {
  const skip = (page - 1) * limit;
  return prisma.friendship.findMany({
    where: { status: "PENDING", addresseeId: userId },
    include: { requester: { select: userSelect() }, addressee: { select: userSelect() } },
    orderBy: { createdAt: "desc" },
    skip,
    take: limit,
  });
}

export function countReceivedRequests(userId) {
  return prisma.friendship.count({ where: { status: "PENDING", addresseeId: userId } });
}

export function findSentRequests(userId, page, limit) {
  const skip = (page - 1) * limit;
  return prisma.friendship.findMany({
    where: { status: "PENDING", requesterId: userId },
    include: { requester: { select: userSelect() }, addressee: { select: userSelect() } },
    orderBy: { createdAt: "desc" },
    skip,
    take: limit,
  });
}

export function countSentRequests(userId) {
  return prisma.friendship.count({ where: { status: "PENDING", requesterId: userId } });
}

export function findFriendActivities(friendIds, page, limit) {
  const skip = (page - 1) * limit;
  return prisma.friendActivity.findMany({
    where: { userId: { in: friendIds } },
    include: { user: { select: userSelect() } },
    orderBy: { createdAt: "desc" },
    skip,
    take: limit,
  });
}

export function countFriendActivities(friendIds) {
  return prisma.friendActivity.count({ where: { userId: { in: friendIds } } });
}

export function createFriendBlock(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('friend_blocks', 'id'), COALESCE((SELECT MAX(id) FROM friend_blocks), 0) + 1, false)",
    );
    return tx.friendBlock.create({ data });
  });
}

export function deleteFriendBlock(userId, blockedUserId) {
  return prisma.friendBlock.deleteMany({
    where: { userId, blockedUserId: Number(blockedUserId) },
  });
}
