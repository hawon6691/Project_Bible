import fs from "node:fs/promises";
import { pathToFileURL } from "node:url";

import { appConfig } from "../../src/config/app.js";
import { buildOpenApiSpec } from "../../src/docs/docs.service.js";
import { getRouteCatalog } from "../../src/routes/index.js";

const testResultsDir = new URL("../../test-results/", import.meta.url);

export async function writeOpenApiArtifacts() {
  const spec = buildOpenApiSpec(appConfig.apiPrefix);
  const routes = getRouteCatalog(appConfig.apiPrefix);

  await fs.mkdir(testResultsDir, { recursive: true });
  await fs.writeFile(
    new URL("openapi.json", testResultsDir),
    JSON.stringify(spec, null, 2),
    "utf8",
  );
  await fs.writeFile(
    new URL("routes.snapshot.json", testResultsDir),
    JSON.stringify(routes, null, 2),
    "utf8",
  );

  console.log(`openapi-exported:${routes.length}`);
  return {
    pathCount: Object.keys(spec.paths).length,
    routeCount: routes.length,
  };
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  await writeOpenApiArtifacts();
}
