import {
  countChatMessages,
  createChatMessage,
  createChatRoom,
  createChatRoomMember,
  findAllChatRooms,
  findChatMessages,
  findChatRoomAccessibleByUser,
  findChatRoomById,
  findChatRoomMember,
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

export async function joinRoom(user, roomId) {
  const room = await findChatRoomById(roomId);
  if (!room) {
    throw notFound("Chat room not found");
  }

  const existingMember = await findChatRoomMember(room.id, user.id);
  if (!existingMember) {
    await createChatRoomMember({
      roomId: room.id,
      userId: user.id,
      joinedAt: new Date(),
    });
  }

  return findChatRoomById(room.id);
}

export async function sendMessage(user, roomId, payload) {
  const room = await findChatRoomAccessibleByUser(user.id, roomId);
  if (!room && user.role !== "ADMIN") {
    throw forbidden("You do not have access to this chat room");
  }

  const adminRoom = room ?? (await findChatRoomById(roomId));
  if (!adminRoom) {
    throw notFound("Chat room not found");
  }

  if (user.role === "ADMIN") {
    const existingMember = await findChatRoomMember(adminRoom.id, user.id);
    if (!existingMember) {
      await createChatRoomMember({
        roomId: adminRoom.id,
        userId: user.id,
        joinedAt: new Date(),
      });
    }
  }

  const message = String(payload?.message ?? "").trim();
  if (!message) {
    throw badRequest("message is required");
  }

  const item = await createChatMessage({
    roomId: adminRoom.id,
    senderId: user.id,
    message,
  });
  await touchChatRoom(adminRoom.id);
  return item;
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
