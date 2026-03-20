import { prisma } from "../prisma.js";

function bumpSequence(tx, tableName) {
  return tx.$executeRawUnsafe(
    `SELECT setval(pg_get_serial_sequence('${tableName}', 'id'), COALESCE((SELECT MAX(id) FROM ${tableName}), 0) + 1, false)`,
  );
}

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
    await bumpSequence(tx, "chat_rooms");
    await bumpSequence(tx, "chat_room_members");

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

export function findChatRoomMember(roomId, userId) {
  return prisma.chatRoomMember.findUnique({
    where: {
      roomId_userId: {
        roomId: Number(roomId),
        userId: Number(userId),
      },
    },
  });
}

export function createChatRoomMember(data) {
  return prisma.$transaction(async (tx) => {
    await bumpSequence(tx, "chat_room_members");
    return tx.chatRoomMember.create({ data });
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

export function createChatMessage(data) {
  return prisma.$transaction(async (tx) => {
    await bumpSequence(tx, "chat_messages");
    return tx.chatMessage.create({
      data,
      include: messageInclude(),
    });
  });
}

export function touchChatRoom(roomId) {
  return prisma.chatRoom.update({
    where: { id: Number(roomId) },
    data: { updatedAt: new Date() },
    include: roomInclude(),
  });
}
