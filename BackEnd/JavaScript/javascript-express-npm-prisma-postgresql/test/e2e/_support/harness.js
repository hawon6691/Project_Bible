import assert from "node:assert/strict";
import { once } from "node:events";
import { createServer } from "node:http";

import { signAccessToken } from "../../../src/auth/token.service.js";
import { createApp } from "../../../src/app.js";
import { attachChatSocket } from "../../../src/chat/chat.socket.js";
import { prisma } from "../../../src/prisma.js";

export async function bootTestApp() {
  const [admin, user] = await Promise.all([
    prisma.user.findFirst({
      where: { role: "ADMIN", deletedAt: null },
      select: { id: true, email: true, role: true },
    }),
    prisma.user.findFirst({
      where: { role: "USER", deletedAt: null },
      select: { id: true, email: true, role: true },
    }),
  ]);

  assert.ok(admin, "Seed admin account is required");
  assert.ok(user, "Seed user account is required");

  const app = createApp();
  const server = createServer(app);
  const io = attachChatSocket(server);
  server.listen(0, "127.0.0.1");
  await once(server, "listening");
  const address = server.address();
  const baseUrl = `http://127.0.0.1:${address.port}`;

  return {
    server,
    io,
    baseUrl,
    admin,
    user,
    adminToken: signAccessToken(admin),
    userToken: signAccessToken(user),
  };
}

export async function shutdownTestApp(context) {
  if (context?.io) {
    await new Promise((resolve) => context.io.close(resolve));
  }
  if (context?.server) {
    await new Promise((resolve) => context.server.close(resolve));
  }
  await prisma.$disconnect();
}

export async function requestJson(baseUrl, path, init = {}) {
  const response = await fetch(`${baseUrl}${path}`, init);
  const text = await response.text();
  return {
    status: response.status,
    body: text ? JSON.parse(text) : null,
    headers: response.headers,
  };
}

export async function requestText(baseUrl, path, init = {}) {
  const response = await fetch(`${baseUrl}${path}`, init);
  return {
    status: response.status,
    body: await response.text(),
    headers: response.headers,
  };
}

export function withBearer(token) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
}
