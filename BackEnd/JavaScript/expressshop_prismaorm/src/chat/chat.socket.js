import { Server } from "socket.io";

import { verifyAccessToken } from "../auth/token.service.js";
import { prisma } from "../prisma.js";
import { sendMessage as sendChatMessage, joinRoom as joinChatRoom } from "./chat.service.js";
import { findChatRoomAccessibleByUser, findChatRoomById } from "./chat.repository.js";

function getRoomName(roomId) {
  return `chat-room:${Number(roomId)}`;
}

function toSocketError(error) {
  return {
    code: error?.code ?? "SOCKET_ERROR",
    message: error instanceof Error ? error.message : "Unexpected socket error",
  };
}

function parseRoomId(value) {
  const roomId = Number(value);
  if (!Number.isInteger(roomId) || roomId <= 0) {
    throw new Error("roomId must be a positive integer");
  }
  return roomId;
}

function parseMessageId(value) {
  const messageId = Number(value);
  if (!Number.isInteger(messageId) || messageId <= 0) {
    throw new Error("messageId must be a positive integer");
  }
  return messageId;
}

function getTokenFromHandshake(socket) {
  const authToken = socket.handshake.auth?.token;
  if (typeof authToken === "string" && authToken.trim()) {
    return authToken.startsWith("Bearer ") ? authToken.slice("Bearer ".length).trim() : authToken.trim();
  }

  const header = socket.handshake.headers.authorization;
  if (typeof header === "string" && header.startsWith("Bearer ")) {
    return header.slice("Bearer ".length).trim();
  }

  return null;
}

async function ensureSocketUser(socket) {
  const token = getTokenFromHandshake(socket);
  if (!token) {
    throw new Error("Authentication required");
  }

  const payload = verifyAccessToken(token);
  const user = await prisma.user.findUnique({
    where: { id: Number(payload.sub) },
  });

  if (!user || user.deletedAt) {
    throw new Error("Authentication required");
  }

  return user;
}

async function ensureRoomAccess(user, roomId) {
  if (user.role === "ADMIN") {
    return findChatRoomById(roomId);
  }

  return findChatRoomAccessibleByUser(user.id, roomId);
}

function emitAck(ack, payload) {
  if (typeof ack === "function") {
    ack(payload);
  }
}

export function attachChatSocket(server) {
  const io = new Server(server, {
    cors: {
      origin: true,
      credentials: true,
    },
  });

  io.use(async (socket, next) => {
    try {
      socket.data.user = await ensureSocketUser(socket);
      next();
    } catch (error) {
      next(error instanceof Error ? error : new Error("Authentication required"));
    }
  });

  io.on("connection", (socket) => {
    socket.on("joinRoom", async (payload = {}, ack) => {
      try {
        const roomId = parseRoomId(payload.roomId);
        await joinChatRoom(socket.data.user, roomId);
        await socket.join(getRoomName(roomId));
        emitAck(ack, {
          success: true,
          data: { roomId },
        });
      } catch (error) {
        emitAck(ack, {
          success: false,
          error: toSocketError(error),
        });
      }
    });

    socket.on("leaveRoom", async (payload = {}, ack) => {
      try {
        const roomId = parseRoomId(payload.roomId);
        await socket.leave(getRoomName(roomId));
        emitAck(ack, {
          success: true,
          data: { roomId },
        });
      } catch (error) {
        emitAck(ack, {
          success: false,
          error: toSocketError(error),
        });
      }
    });

    socket.on("sendMessage", async (payload = {}, ack) => {
      try {
        const roomId = parseRoomId(payload.roomId);
        const content = String(payload.content ?? payload.message ?? "").trim();
        const message = await sendChatMessage(socket.data.user, roomId, { message: content });
        const event = {
          id: message.id,
          roomId: message.roomId,
          senderId: message.senderId,
          content: message.message,
          createdAt: message.createdAt,
        };

        io.to(getRoomName(roomId)).emit("newMessage", event);
        emitAck(ack, {
          success: true,
          data: event,
        });
      } catch (error) {
        emitAck(ack, {
          success: false,
          error: toSocketError(error),
        });
      }
    });

    socket.on("messageRead", async (payload = {}, ack) => {
      try {
        const roomId = parseRoomId(payload.roomId);
        const messageId = parseMessageId(payload.messageId);
        const room = await ensureRoomAccess(socket.data.user, roomId);
        if (!room) {
          throw new Error("Chat room not found or inaccessible");
        }

        socket.to(getRoomName(roomId)).emit("readReceipt", {
          roomId,
          messageId,
          readBy: socket.data.user.id,
        });
        emitAck(ack, {
          success: true,
          data: { roomId, messageId },
        });
      } catch (error) {
        emitAck(ack, {
          success: false,
          error: toSocketError(error),
        });
      }
    });

    socket.on("typing", async (payload = {}, ack) => {
      try {
        const roomId = parseRoomId(payload.roomId);
        const room = await ensureRoomAccess(socket.data.user, roomId);
        if (!room) {
          throw new Error("Chat room not found or inaccessible");
        }

        socket.to(getRoomName(roomId)).emit("userTyping", {
          roomId,
          userId: socket.data.user.id,
        });
        emitAck(ack, {
          success: true,
          data: { roomId },
        });
      } catch (error) {
        emitAck(ack, {
          success: false,
          error: toSocketError(error),
        });
      }
    });
  });

  return io;
}
