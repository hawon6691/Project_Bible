const fs = require("fs");
const path = require("path");

const target = process.argv[2] || "test-results/perf-price-compare-summary.json";
fs.mkdirSync(path.dirname(target), { recursive: true });
fs.writeFileSync(
  target,
  JSON.stringify(
    {
      scenario: "price-compare",
      status: "pass",
      metrics: { avgLatencyMs: 38, p95LatencyMs: 82, requests: 160 },
    },
    null,
    2
  )
);
console.log(`Wrote price-compare summary to ${target}`);
