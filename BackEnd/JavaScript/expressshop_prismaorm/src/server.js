import dotenv from "dotenv";
import { createServer } from "node:http";

import { appConfig } from "./config/app.js";
import { createApp } from "./app.js";
import { attachChatSocket } from "./chat/chat.socket.js";

dotenv.config();

const app = createApp();
const server = createServer(app);
attachChatSocket(server);

server.listen(appConfig.port, () => {
  console.log(`PBShop JavaScript Express Prisma ORM listening on ${appConfig.port}`);
});
