import assert from "node:assert/strict";
import test, { after, before } from "node:test";

import {
  bootTestApp,
  requestJson,
  shutdownTestApp,
  withBearer,
} from "../e2e/_support/harness.js";

let context;

before(async () => {
  context = await bootTestApp();
});

after(async () => {
  await shutdownTestApp(context);
});

test("Activity APIs return authenticated activity snapshots", async () => {
  const viewsResult = await requestJson(
    context.baseUrl,
    "/api/v1/activity/views",
    withBearer(context.userToken),
  );
  const searchesResult = await requestJson(
    context.baseUrl,
    "/api/v1/activity/searches",
    withBearer(context.userToken),
  );

  assert.equal(viewsResult.status, 200);
  assert.equal(searchesResult.status, 200);
  assert.ok(Array.isArray(viewsResult.body.data));
  assert.ok(Array.isArray(searchesResult.body.data));
});

test("Chat APIs create room and send message", async () => {
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
        name: "api chat room",
        participantUserIds: [context.admin.id],
        isPrivate: true,
      }),
    },
  );

  assert.equal(createRoomResult.status, 201);
  const roomId = createRoomResult.body.data.id;

  const roomsResult = await requestJson(
    context.baseUrl,
    "/api/v1/chat/rooms",
    withBearer(context.userToken),
  );
  const sendMessageResult = await requestJson(
    context.baseUrl,
    `/api/v1/chat/rooms/${roomId}/messages`,
    {
      method: "POST",
      headers: {
        ...withBearer(context.userToken).headers,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: "api-domain-message",
      }),
    },
  );

  assert.equal(roomsResult.status, 200);
  assert.ok(Array.isArray(roomsResult.body.data));
  assert.equal(sendMessageResult.status, 201);
  assert.equal(sendMessageResult.body.data.roomId, roomId);
});

test("Push APIs update and return preference payload", async () => {
  const updateResult = await requestJson(
    context.baseUrl,
    "/api/v1/push/preferences",
    {
      method: "POST",
      headers: {
        ...withBearer(context.userToken).headers,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        priceAlertEnabled: true,
        orderStatusEnabled: true,
      }),
    },
  );
  const getResult = await requestJson(
    context.baseUrl,
    "/api/v1/push/preferences",
    withBearer(context.userToken),
  );

  assert.equal(updateResult.status, 200);
  assert.equal(updateResult.body.success, true);
  assert.equal(getResult.status, 200);
  assert.equal(getResult.body.success, true);
  assert.equal(typeof getResult.body.data, "object");
});
