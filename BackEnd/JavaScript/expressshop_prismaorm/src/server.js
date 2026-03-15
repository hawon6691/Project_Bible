import dotenv from "dotenv";

import { appConfig } from "./config/app.js";
import { createApp } from "./app.js";

dotenv.config();

const app = createApp();

app.listen(appConfig.port, () => {
  console.log(`PBShop JavaScript Express Prisma ORM listening on ${appConfig.port}`);
});
