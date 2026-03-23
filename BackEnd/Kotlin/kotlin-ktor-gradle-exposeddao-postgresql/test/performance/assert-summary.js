const fs = require("fs");

const target = process.argv[2];
if (!target) {
  console.error("Usage: node assert-summary.js <summary.json>");
  process.exit(1);
}

const summary = JSON.parse(fs.readFileSync(target, "utf8"));
if (summary.status !== "pass") {
  console.error(`Performance summary failed: ${target}`);
  process.exit(1);
}

console.log(`Performance summary passed: ${target}`);
