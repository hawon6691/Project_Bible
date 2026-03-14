import dotenv from "dotenv";

import { createApp } from "./app.js";

dotenv.config();

const port = Number(process.env.PORT ?? 8000);
const app = createApp();

app.listen(port, () => {
  console.log(`PBShop JavaScript Express Prisma ORM listening on ${port}`);
});
