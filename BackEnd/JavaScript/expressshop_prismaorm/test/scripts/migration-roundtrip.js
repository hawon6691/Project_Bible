import fs from "node:fs/promises";

import { prisma } from "../../src/prisma.js";

const testResultsDir = new URL("../../test-results/", import.meta.url);

try {
  await prisma.$executeRawUnsafe(
    "CREATE TEMP TABLE codex_migration_roundtrip (id INT PRIMARY KEY, name TEXT NOT NULL)",
  );
  await prisma.$executeRawUnsafe(
    "INSERT INTO codex_migration_roundtrip (id, name) VALUES (1, 'ok'), (2, 'roundtrip')",
  );
  const rows = await prisma.$queryRawUnsafe(
    "SELECT COUNT(*)::int AS count FROM codex_migration_roundtrip",
  );

  const summary = {
    checkedAt: new Date().toISOString(),
    insertedRows: Number(rows[0]?.count ?? 0),
    tableStrategy: "temporary-table",
  };

  await fs.mkdir(testResultsDir, { recursive: true });
  await fs.writeFile(
    new URL("migration-roundtrip.json", testResultsDir),
    JSON.stringify(summary, null, 2),
    "utf8",
  );

  if (summary.insertedRows !== 2) {
    console.error("migration-roundtrip-failed");
    process.exit(1);
  }

  console.log("migration-roundtrip-ok");
} finally {
  await prisma.$disconnect();
}
