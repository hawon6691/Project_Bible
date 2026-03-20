import {
  acceptFriendRequest,
  blockUser,
  getFriendFeed,
  getFriends,
  getReceivedRequests,
  getSentRequests,
  rejectFriendRequest,
  removeFriend,
  requestFriend,
  unblockUser,
} from "./friend.service.js";
import {
  toFriendActivityDto,
  toFriendRequestDto,
  toFriendshipDto,
} from "./friend.mapper.js";
import { success } from "../utils/response.js";

export async function requestFriendController(req, res) {
  const data = await requestFriend(req.user.id, req.params.userId);
  res.status(201).json(success(data));
}

export async function acceptFriendRequestController(req, res) {
  const data = await acceptFriendRequest(req.user.id, req.params.friendshipId);
  res.status(200).json(success(data));
}

export async function rejectFriendRequestController(req, res) {
  const data = await rejectFriendRequest(req.user.id, req.params.friendshipId);
  res.status(200).json(success(data));
}

export async function getFriendsController(req, res) {
  const { items, meta } = await getFriends(req.user.id, req.query);
  res.status(200).json(success(items.map((item) => toFriendshipDto(item, req.user.id)), meta));
}

export async function getReceivedRequestsController(req, res) {
  const { items, meta } = await getReceivedRequests(req.user.id, req.query);
  res.status(200).json(success(items.map(toFriendRequestDto), meta));
}

export async function getSentRequestsController(req, res) {
  const { items, meta } = await getSentRequests(req.user.id, req.query);
  res.status(200).json(success(items.map(toFriendRequestDto), meta));
}

export async function getFriendFeedController(req, res) {
  const { items, meta } = await getFriendFeed(req.user.id, req.query);
  res.status(200).json(success(items.map(toFriendActivityDto), meta));
}

export async function blockUserController(req, res) {
  const data = await blockUser(req.user.id, req.params.userId);
  res.status(200).json(success(data));
}

export async function unblockUserController(req, res) {
  const data = await unblockUser(req.user.id, req.params.userId);
  res.status(200).json(success(data));
}

export async function removeFriendController(req, res) {
  const data = await removeFriend(req.user.id, req.params.userId);
  res.status(200).json(success(data));
}
