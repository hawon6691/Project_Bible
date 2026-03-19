import fs from "node:fs/promises";

import { prisma } from "../../src/prisma.js";

const testResultsDir = new URL("../../test-results/", import.meta.url);

const requiredTables = [
  "users",
  "products",
  "product_query_views",
  "search_index_outbox",
  "chat_rooms",
  "chat_messages",
];

const requiredColumns = [
  ["users", "email"],
  ["products", "name"],
  ["product_query_views", "product_id"],
  ["search_index_outbox", "event_type"],
  ["chat_rooms", "name"],
  ["chat_messages", "message"],
];

try {
  const existingTables = await prisma.$queryRawUnsafe(
    `SELECT table_name
       FROM information_schema.tables
      WHERE table_schema = 'public'
        AND table_name IN (${requiredTables.map((item) => `'${item}'`).join(", ")})
      ORDER BY table_name`,
  );

  const missingTables = requiredTables.filter(
    (name) => !existingTables.some((row) => row.table_name === name),
  );

  const columnChecks = [];
  for (const [tableName, columnName] of requiredColumns) {
    const rows = await prisma.$queryRawUnsafe(
      `SELECT COUNT(*)::int AS count
         FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = '${tableName}'
          AND column_name = '${columnName}'`,
    );
    columnChecks.push({
      tableName,
      columnName,
      exists: Number(rows[0]?.count ?? 0) > 0,
    });
  }

  const missingColumns = columnChecks.filter((item) => !item.exists);
  const summary = {
    checkedAt: new Date().toISOString(),
    missingTables,
    missingColumns,
    validatedTableCount: requiredTables.length,
    validatedColumnCount: requiredColumns.length,
  };

  await fs.mkdir(testResultsDir, { recursive: true });
  await fs.writeFile(
    new URL("migration-validation.json", testResultsDir),
    JSON.stringify(summary, null, 2),
    "utf8",
  );

  if (missingTables.length > 0 || missingColumns.length > 0) {
    console.error("migration-validation-failed");
    process.exit(1);
  }

  console.log("migration-validation-ok");
} finally {
  await prisma.$disconnect();
}
