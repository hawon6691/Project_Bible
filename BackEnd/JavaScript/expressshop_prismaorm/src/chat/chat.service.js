import {
  countChatMessages,
  createChatRoom,
  findAllChatRooms,
  findChatMessages,
  findChatRoomAccessibleByUser,
  findChatRoomById,
  findChatRoomsForUser,
  touchChatRoom,
} from "./chat.repository.js";
import { badRequest, forbidden, notFound } from "../utils/http-error.js";

export async function createRoom(user, payload) {
  const name = payload?.name?.trim();
  const participantUserIds = Array.isArray(payload?.participantUserIds)
    ? payload.participantUserIds.map((id) => Number(id)).filter(Boolean)
    : [];
  const uniqueMemberIds = [...new Set([user.id, ...participantUserIds])];

  if (!name) {
    throw badRequest("name is required");
  }

  return createChatRoom({
    name,
    createdBy: user.id,
    isPrivate: payload?.isPrivate ?? true,
    members: {
      create: uniqueMemberIds.map((userId) => ({
        userId,
        joinedAt: new Date(),
      })),
    },
  });
}

export async function getRooms(user) {
  const items =
    user.role === "ADMIN"
      ? await findAllChatRooms()
      : await findChatRoomsForUser(user.id);
  return { items, meta: { total: items.length } };
}

export async function getRoomMessages(user, roomId, query) {
  const room =
    user.role === "ADMIN"
      ? await findChatRoomById(roomId)
      : await findChatRoomAccessibleByUser(user.id, roomId);

  if (!room) {
    throw notFound("Chat room not found");
  }

  const page = Math.max(Number(query?.page ?? 1), 1);
  const limit = Math.min(Math.max(Number(query?.limit ?? 20), 1), 100);
  const [items, total] = await Promise.all([
    findChatMessages(room.id, page, limit),
    countChatMessages(room.id),
  ]);

  return { items, meta: { total, page, limit } };
}

export async function closeRoom(user, roomId) {
  const room = await findChatRoomById(roomId);
  if (!room) {
    throw notFound("Chat room not found");
  }

  const isMember = room.members.some((member) => member.userId === user.id);
  if (user.role !== "ADMIN" && room.createdBy !== user.id && !isMember) {
    throw forbidden("You cannot close this chat room");
  }

  return touchChatRoom(room.id);
}
