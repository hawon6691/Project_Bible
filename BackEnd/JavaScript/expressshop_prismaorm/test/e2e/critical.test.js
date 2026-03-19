import assert from "node:assert/strict";
import test, { after, before } from "node:test";
import { io as createSocketClient } from "socket.io-client";

import {
  bootTestApp,
  requestJson,
  requestText,
  shutdownTestApp,
  withBearer,
} from "./_support/harness.js";

let context;

function emitWithAck(socket, event, payload) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      reject(new Error(`Timed out waiting for ack from ${event}`));
    }, 5000);

    socket.emit(event, payload, (response) => {
      clearTimeout(timer);
      resolve(response);
    });
  });
}

function waitForSocketEvent(socket, event) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      socket.off(event, onEvent);
      reject(new Error(`Timed out waiting for ${event}`));
    }, 5000);

    function onEvent(payload) {
      clearTimeout(timer);
      resolve(payload);
    }

    socket.once(event, onEvent);
  });
}

function connectSocket(baseUrl, token) {
  return new Promise((resolve, reject) => {
    const socket = createSocketClient(baseUrl, {
      transports: ["websocket"],
      auth: {
        token,
      },
    });

    const timer = setTimeout(() => {
      socket.close();
      reject(new Error("Timed out waiting for socket connection"));
    }, 5000);

    socket.once("connect", () => {
      clearTimeout(timer);
      resolve(socket);
    });

    socket.once("connect_error", (error) => {
      clearTimeout(timer);
      reject(error);
    });
  });
}

before(async () => {
  context = await bootTestApp();
});

after(async () => {
  await shutdownTestApp(context);
});

test("GET /health returns UP", async () => {
  const result = await requestJson(context.baseUrl, "/health");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.status, "UP");
});

test("GET /api/v1/docs-status returns available docs wiring state", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/docs-status");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.swagger, "available");
  assert.equal(result.body.data.openapi, "available");
  assert.equal(result.body.data.openapiPath, "/docs/openapi");
  assert.equal(result.body.data.swaggerPath, "/docs/swagger");
});

test("GET /docs/openapi exposes OpenAPI document", async () => {
  const result = await requestJson(context.baseUrl, "/docs/openapi");

  assert.equal(result.status, 200);
  assert.equal(typeof result.body.openapi, "string");
  assert.equal(result.body.info.title, "PBShop JavaScript Express Prisma API");
  assert.ok(result.body.paths["/api/v1/products"]);
  assert.ok(result.body.paths["/api/v1/errors/codes"]);
  assert.ok(result.body.paths["/api/v1/errors/codes/{key}"]);
  assert.ok(result.body.paths["/api/v1/query/products"]);
  assert.ok(result.body.paths["/api/v1/query/products/{productId}"]);
  assert.ok(result.body.paths["/api/v1/admin/query/products/{productId}/sync"]);
  assert.ok(result.body.paths["/api/v1/admin/query/products/rebuild"]);
  assert.ok(result.body.paths["/api/v1/chat/rooms/{id}/join"]);
  assert.ok(result.body.paths["/api/v1/chat/rooms/{id}/messages"]);
});

test("GET /docs/swagger exposes Swagger UI page", async () => {
  const result = await requestText(context.baseUrl, "/docs/swagger");

  assert.equal(result.status, 200);
  assert.match(result.body, /Swagger UI/);
});

test("GET /api/v1/products returns public product list", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/products");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
});

test("GET /api/v1/errors/codes returns public error code catalog", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/errors/codes");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.total, "number");
  assert.ok(Array.isArray(result.body.data.items));
  assert.ok(result.body.data.items.length > 0);
  assert.equal(typeof result.body.data.items[0].key, "string");
  assert.equal(typeof result.body.data.items[0].code, "string");
  assert.equal(typeof result.body.data.items[0].message, "string");
});

test("GET /api/v1/errors/codes/:key returns a matching error code item", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/errors/codes/NOT_FOUND");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.key, "NOT_FOUND");
  assert.equal(result.body.data.code, "HTTP_404");
  assert.equal(typeof result.body.data.message, "string");
});

test("GET /api/v1/errors/codes/:key returns null for unknown keys", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/errors/codes/UNKNOWN_CODE");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data, null);
});

test("GET /api/v1/query/products returns public query product views", async () => {
  const syncResult = await requestJson(
    context.baseUrl,
    "/api/v1/admin/query/products/1/sync",
    {
      method: "POST",
      ...withBearer(context.adminToken),
    },
  );

  assert.equal(syncResult.status, 200);
  assert.equal(syncResult.body.success, true);

  const result = await requestJson(context.baseUrl, "/api/v1/query/products?page=1&limit=5");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
  assert.equal(typeof result.body.meta.total, "number");
  assert.ok(result.body.meta.total >= 1);
});

test("GET /api/v1/query/products/:productId returns a synced query product view", async () => {
  const syncResult = await requestJson(
    context.baseUrl,
    "/api/v1/admin/query/products/1/sync",
    {
      method: "POST",
      ...withBearer(context.adminToken),
    },
  );

  assert.equal(syncResult.status, 200);

  const result = await requestJson(context.baseUrl, "/api/v1/query/products/1");

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.productId, 1);
  assert.equal(typeof result.body.data.name, "string");
});

test("POST /api/v1/admin/query/products/:productId/sync allows admin user", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/query/products/1/sync",
    {
      method: "POST",
      ...withBearer(context.adminToken),
    },
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.productId, 1);
  assert.equal(typeof result.body.data.syncedAt, "string");
});

test("POST /api/v1/admin/query/products/rebuild allows admin user", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/query/products/rebuild",
    {
      method: "POST",
      ...withBearer(context.adminToken),
    },
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.syncedCount, "number");
  assert.ok(result.body.data.syncedCount >= 1);
});

test("POST /api/v1/chat/rooms/:id/join lets another authenticated user join a room", async () => {
  const createRoomResult = await requestJson(
    context.baseUrl,
    "/api/v1/chat/rooms",
    {
      method: "POST",
      ...withBearer(context.userToken),
      headers: {
        ...withBearer(context.userToken).headers,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        name: "critical chat room",
        participantUserIds: [],
        isPrivate: true,
      }),
    },
  );

  assert.equal(createRoomResult.status, 201);
  assert.equal(createRoomResult.body.success, true);

  const roomId = createRoomResult.body.data.id;
  const joinResult = await requestJson(
    context.baseUrl,
    `/api/v1/chat/rooms/${roomId}/join`,
    {
      method: "POST",
      ...withBearer(context.adminToken),
    },
  );

  assert.equal(joinResult.status, 200);
  assert.equal(joinResult.body.success, true);
  assert.equal(joinResult.body.data.id, roomId);
});

test("POST /api/v1/chat/rooms/:id/messages sends a chat message", async () => {
  const createRoomResult = await requestJson(
    context.baseUrl,
    "/api/v1/chat/rooms",
    {
      method: "POST",
      ...withBearer(context.userToken),
      headers: {
        ...withBearer(context.userToken).headers,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        name: "message room",
        participantUserIds: [],
        isPrivate: true,
      }),
    },
  );

  assert.equal(createRoomResult.status, 201);

  const roomId = createRoomResult.body.data.id;
  const sendResult = await requestJson(
    context.baseUrl,
    `/api/v1/chat/rooms/${roomId}/messages`,
    {
      method: "POST",
      headers: {
        ...withBearer(context.userToken).headers,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: "critical hello",
      }),
    },
  );

  assert.equal(sendResult.status, 201);
  assert.equal(sendResult.body.success, true);
  assert.equal(sendResult.body.data.roomId, roomId);
  assert.equal(sendResult.body.data.senderId, context.user.id);
  assert.equal(sendResult.body.data.message, "critical hello");
});

test("Socket.IO chat events join, send, typing, read, leave work with JWT auth", async () => {
  const createRoomResult = await requestJson(
    context.baseUrl,
    "/api/v1/chat/rooms",
    {
      method: "POST",
      headers: {
        ...withBearer(context.userToken).headers,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        name: "socket room",
        participantUserIds: [],
        isPrivate: true,
      }),
    },
  );

  assert.equal(createRoomResult.status, 201);
  const roomId = createRoomResult.body.data.id;

  const userSocket = await connectSocket(context.baseUrl, context.userToken);
  const adminSocket = await connectSocket(context.baseUrl, context.adminToken);

  try {
    const userJoinAck = await emitWithAck(userSocket, "joinRoom", { roomId });
    assert.equal(userJoinAck.success, true);
    assert.equal(userJoinAck.data.roomId, roomId);

    const adminJoinAck = await emitWithAck(adminSocket, "joinRoom", { roomId });
    assert.equal(adminJoinAck.success, true);
    assert.equal(adminJoinAck.data.roomId, roomId);

    const typingEventPromise = waitForSocketEvent(adminSocket, "userTyping");
    const typingAck = await emitWithAck(userSocket, "typing", { roomId });
    assert.equal(typingAck.success, true);
    const typingEvent = await typingEventPromise;
    assert.equal(typingEvent.roomId, roomId);
    assert.equal(typingEvent.userId, context.user.id);

    const newMessagePromise = waitForSocketEvent(adminSocket, "newMessage");
    const sendAck = await emitWithAck(userSocket, "sendMessage", {
      roomId,
      content: "socket hello",
    });
    assert.equal(sendAck.success, true);
    assert.equal(sendAck.data.roomId, roomId);
    assert.equal(sendAck.data.senderId, context.user.id);
    assert.equal(sendAck.data.content, "socket hello");

    const newMessageEvent = await newMessagePromise;
    assert.equal(newMessageEvent.roomId, roomId);
    assert.equal(newMessageEvent.senderId, context.user.id);
    assert.equal(newMessageEvent.content, "socket hello");

    const readReceiptPromise = waitForSocketEvent(userSocket, "readReceipt");
    const readAck = await emitWithAck(adminSocket, "messageRead", {
      roomId,
      messageId: newMessageEvent.id,
    });
    assert.equal(readAck.success, true);
    const readReceiptEvent = await readReceiptPromise;
    assert.equal(readReceiptEvent.roomId, roomId);
    assert.equal(readReceiptEvent.messageId, newMessageEvent.id);
    assert.equal(readReceiptEvent.readBy, context.admin.id);

    const leaveAck = await emitWithAck(adminSocket, "leaveRoom", { roomId });
    assert.equal(leaveAck.success, true);
    assert.equal(leaveAck.data.roomId, roomId);
  } finally {
    userSocket.close();
    adminSocket.close();
  }
});

test("GET /api/v1/users/me requires auth", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/users/me");

  assert.equal(result.status, 401);
  assert.equal(result.body.success, false);
});

test("GET /api/v1/users/me returns current user with bearer token", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/users/me",
    withBearer(context.userToken),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(result.body.data.role, "USER");
});

test("GET /api/v1/admin/observability/metrics rejects non-admin user", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/observability/metrics",
    withBearer(context.userToken),
  );

  assert.equal(result.status, 403);
  assert.equal(result.body.success, false);
});

test("GET /api/v1/admin/observability/metrics allows admin user", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/admin/observability/metrics",
    withBearer(context.adminToken),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.equal(typeof result.body.data.totalRequests, "number");
});

test("GET /api/v1/pc-builds returns current user build list", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/pc-builds?page=1&limit=5",
    withBearer(context.userToken),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
  assert.equal(typeof result.body.meta.total, "number");
});

test("GET /api/v1/images/:id/variants returns 404 for missing image", async () => {
  const result = await requestJson(context.baseUrl, "/api/v1/images/999999/variants");

  assert.equal(result.status, 404);
  assert.equal(result.body.success, false);
});
