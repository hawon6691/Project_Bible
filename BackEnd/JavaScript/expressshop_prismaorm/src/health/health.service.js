import { pingDatabase } from "./health.repository.js";

export async function getHealth() {
  await pingDatabase();
  return {
    status: "UP",
    app: process.env.APP_NAME ?? "javascript-express-prisma",
    database: "reachable",
  };
}

export async function getApiHealth() {
  await pingDatabase();
  return { status: "UP" };
}

export function getDocsStatus() {
  return {
    swagger: "pending",
    openapi: "pending",
    message: "Swagger/OpenAPI wiring is the next setup step.",
  };
}
