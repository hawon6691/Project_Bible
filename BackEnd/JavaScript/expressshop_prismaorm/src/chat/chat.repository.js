import { prisma } from "../prisma.js";

function roomInclude() {
  return {
    creator: {
      select: { id: true, email: true, name: true, role: true },
    },
    members: {
      include: {
        user: {
          select: { id: true, email: true, name: true, role: true },
        },
      },
      orderBy: { id: "asc" },
    },
  };
}

function messageInclude() {
  return {
    sender: {
      select: { id: true, email: true, name: true, role: true },
    },
  };
}

export function createChatRoom(data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('chat_rooms', 'id'), COALESCE((SELECT MAX(id) FROM chat_rooms), 0) + 1, false)",
    );
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('chat_room_members', 'id'), COALESCE((SELECT MAX(id) FROM chat_room_members), 0) + 1, false)",
    );

    return tx.chatRoom.create({
      data,
      include: roomInclude(),
    });
  });
}

export function findChatRoomsForUser(userId) {
  return prisma.chatRoom.findMany({
    where: {
      OR: [{ createdBy: userId }, { members: { some: { userId } } }],
    },
    include: roomInclude(),
    orderBy: [{ updatedAt: "desc" }, { id: "desc" }],
  });
}

export function findAllChatRooms() {
  return prisma.chatRoom.findMany({
    include: roomInclude(),
    orderBy: [{ updatedAt: "desc" }, { id: "desc" }],
  });
}

export function findChatRoomAccessibleByUser(userId, roomId) {
  return prisma.chatRoom.findFirst({
    where: {
      id: Number(roomId),
      OR: [{ createdBy: userId }, { members: { some: { userId } } }],
    },
    include: roomInclude(),
  });
}

export function findChatRoomById(roomId) {
  return prisma.chatRoom.findUnique({
    where: { id: Number(roomId) },
    include: roomInclude(),
  });
}

export function findChatMessages(roomId, page = 1, limit = 20) {
  const skip = (page - 1) * limit;
  return prisma.chatMessage.findMany({
    where: { roomId: Number(roomId) },
    include: messageInclude(),
    orderBy: [{ createdAt: "desc" }, { id: "desc" }],
    skip,
    take: limit,
  });
}

export function countChatMessages(roomId) {
  return prisma.chatMessage.count({ where: { roomId: Number(roomId) } });
}

export function touchChatRoom(roomId) {
  return prisma.chatRoom.update({
    where: { id: Number(roomId) },
    data: { updatedAt: new Date() },
    include: roomInclude(),
  });
}
