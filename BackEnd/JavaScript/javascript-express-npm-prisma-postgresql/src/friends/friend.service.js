import {
  countAcceptedFriends,
  countFriendActivities,
  countReceivedRequests,
  countSentRequests,
  createFriendBlock,
  createFriendship,
  deleteFriendBlock,
  deleteFriendshipPair,
  findAcceptedFriends,
  findFriendActivities,
  findFriendshipById,
  findFriendshipPair,
  findReceivedRequests,
  findSentRequests,
  updateFriendship,
} from "./friend.repository.js";
import { badRequest, forbidden, notFound } from "../utils/http-error.js";

function pagination(query) {
  return {
    page: Math.max(Number(query?.page ?? 1), 1),
    limit: Math.min(Math.max(Number(query?.limit ?? 20), 1), 100),
  };
}

export async function requestFriend(userId, targetUserId) {
  const targetId = Number(targetUserId);
  if (!targetId || targetId === userId) throw badRequest("target user is invalid");
  const existing = await findFriendshipPair(userId, targetId);
  if (existing) throw badRequest("friendship already exists");
  await createFriendship({ requesterId: userId, addresseeId: targetId, status: "PENDING" });
  return { message: "Friend request sent" };
}

export async function acceptFriendRequest(userId, friendshipId) {
  const friendship = await findFriendshipById(friendshipId);
  if (!friendship) throw notFound("Friend request not found");
  if (friendship.addresseeId !== userId) throw forbidden("You cannot accept this request");
  await updateFriendship(friendshipId, { status: "ACCEPTED" });
  return { message: "Friend request accepted" };
}

export async function rejectFriendRequest(userId, friendshipId) {
  const friendship = await findFriendshipById(friendshipId);
  if (!friendship) throw notFound("Friend request not found");
  if (friendship.addresseeId !== userId) throw forbidden("You cannot reject this request");
  await updateFriendship(friendshipId, { status: "BLOCKED" });
  return { message: "Friend request rejected" };
}

export async function getFriends(userId, query) {
  const { page, limit } = pagination(query);
  const [items, total] = await Promise.all([
    findAcceptedFriends(userId, page, limit),
    countAcceptedFriends(userId),
  ]);
  return { items, meta: { total, page, limit } };
}

export async function getReceivedRequests(userId, query) {
  const { page, limit } = pagination(query);
  const [items, total] = await Promise.all([
    findReceivedRequests(userId, page, limit),
    countReceivedRequests(userId),
  ]);
  return { items, meta: { total, page, limit } };
}

export async function getSentRequests(userId, query) {
  const { page, limit } = pagination(query);
  const [items, total] = await Promise.all([
    findSentRequests(userId, page, limit),
    countSentRequests(userId),
  ]);
  return { items, meta: { total, page, limit } };
}

export async function getFriendFeed(userId, query) {
  const friendships = await findAcceptedFriends(userId, 1, 500);
  const friendIds = friendships.map((item) =>
    item.requesterId === userId ? item.addresseeId : item.requesterId,
  );
  const { page, limit } = pagination(query);
  const [items, total] = await Promise.all([
    findFriendActivities(friendIds, page, limit),
    countFriendActivities(friendIds),
  ]);
  return { items, meta: { total, page, limit } };
}

export async function blockUser(userId, blockedUserId) {
  await createFriendBlock({ userId, blockedUserId: Number(blockedUserId) });
  return { message: "User blocked" };
}

export async function unblockUser(userId, blockedUserId) {
  await deleteFriendBlock(userId, blockedUserId);
  return { message: "User unblocked" };
}

export async function removeFriend(userId, targetUserId) {
  const result = await deleteFriendshipPair(userId, targetUserId);
  if (result.count === 0) throw notFound("Friend not found");
  return { message: "Friend removed" };
}
