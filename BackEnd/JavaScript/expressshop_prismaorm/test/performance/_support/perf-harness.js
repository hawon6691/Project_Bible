import { createServer } from "node:http";
import { once } from "node:events";

import { createApp } from "../../../src/app.js";
import { attachChatSocket } from "../../../src/chat/chat.socket.js";
import { prisma } from "../../../src/prisma.js";

export async function bootPerfApp() {
  const app = createApp();
  const server = createServer(app);
  const io = attachChatSocket(server);

  server.listen(0, "127.0.0.1");
  await once(server, "listening");
  const address = server.address();

  return {
    io,
    server,
    baseUrl: `http://127.0.0.1:${address.port}`,
  };
}

export async function shutdownPerfApp(context) {
  if (context?.io) {
    await new Promise((resolve) => context.io.close(resolve));
  }
  if (context?.server) {
    await new Promise((resolve) => context.server.close(resolve));
  }
  await prisma.$disconnect();
}

export async function measurePath(baseUrl, path, iterations) {
  const samples = [];

  for (let index = 0; index < iterations; index += 1) {
    const startedAt = Date.now();
    const response = await fetch(`${baseUrl}${path}`);
    const durationMs = Date.now() - startedAt;
    samples.push({
      durationMs,
      status: response.status,
    });
  }

  return samples;
}
