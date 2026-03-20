import fs from "node:fs/promises";

import { runCommand } from "./_support/run-command.js";

const testResultsDir = new URL("../../test-results/", import.meta.url);
const runs = [];

for (let index = 0; index < 2; index += 1) {
  const result = await runCommand(
    ["--test", "test/e2e/critical.test.js"],
    { command: process.execPath },
  );
  runs.push({
    run: index + 1,
    code: result.code,
    durationMs: result.durationMs,
  });

  if (result.code !== 0) {
    break;
  }
}

await fs.mkdir(testResultsDir, { recursive: true });
await fs.writeFile(
  new URL("stability-check.json", testResultsDir),
  JSON.stringify(
    {
      checkedAt: new Date().toISOString(),
      runs,
      stable: runs.every((item) => item.code === 0),
    },
    null,
    2,
  ),
  "utf8",
);

if (runs.some((item) => item.code !== 0)) {
  console.error("stability-check-failed");
  process.exit(1);
}

console.log("stability-check-ok");
