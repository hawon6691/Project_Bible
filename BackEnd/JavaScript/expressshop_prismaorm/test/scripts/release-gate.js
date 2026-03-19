import fs from "node:fs/promises";

import { runCommand } from "./_support/run-command.js";

const testResultsDir = new URL("../../test-results/", import.meta.url);

const commands = [
  "test:quality",
  "test:e2e:critical",
  "test:e2e:ops",
  "test:contract:doc",
  "test:script:validate-migrations",
];

const results = [];

for (const scriptName of commands) {
  const result = await runCommand(["run", scriptName]);
  results.push({
    scriptName,
    code: result.code,
    durationMs: result.durationMs,
  });

  if (result.code !== 0) {
    break;
  }
}

await fs.mkdir(testResultsDir, { recursive: true });
await fs.writeFile(
  new URL("release-gate-summary.json", testResultsDir),
  JSON.stringify(
    {
      checkedAt: new Date().toISOString(),
      passed: results.every((item) => item.code === 0),
      results,
    },
    null,
    2,
  ),
  "utf8",
);

if (results.some((item) => item.code !== 0)) {
  console.error("release-gate-failed");
  process.exit(1);
}

console.log("release-gate-ok");
