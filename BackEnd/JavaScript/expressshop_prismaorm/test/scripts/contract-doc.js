import fs from "node:fs/promises";

import { runCommand } from "./_support/run-command.js";
import { writeOpenApiArtifacts } from "./export-openapi.js";

const testResultsDir = new URL("../../test-results/", import.meta.url);

const contractRun = await runCommand(
  ["--test", "test/e2e/contract.test.js"],
  { command: process.execPath },
);
if (contractRun.code !== 0) {
  process.exit(contractRun.code);
}

const openApi = await writeOpenApiArtifacts();

await fs.mkdir(testResultsDir, { recursive: true });
await fs.writeFile(
  new URL("contract-doc-summary.json", testResultsDir),
  JSON.stringify(
    {
      generatedAt: new Date().toISOString(),
      contractExitCode: contractRun.code,
      ...openApi,
    },
    null,
    2,
  ),
  "utf8",
);

console.log("contract-doc-ready");
