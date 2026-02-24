const fs = require('fs');

const [summaryPath, profile = 'default'] = process.argv.slice(2);

if (!summaryPath) {
  console.error('Usage: node test/performance/assert-summary.js <summaryPath> [profile]');
  process.exit(1);
}

if (!fs.existsSync(summaryPath)) {
  console.error(`Summary file not found: ${summaryPath}`);
  process.exit(1);
}

const summary = JSON.parse(fs.readFileSync(summaryPath, 'utf8'));
const durationValues = summary?.metrics?.http_req_duration?.values ?? {};
const failedValues = summary?.metrics?.http_req_failed?.values ?? {};

const p95 = Number(durationValues['p(95)'] ?? Number.POSITIVE_INFINITY);
const p99 = Number(durationValues['p(99)'] ?? Number.POSITIVE_INFINITY);
const failedRate = Number(failedValues.rate ?? Number.POSITIVE_INFINITY);

const profiles = {
  default: { p95: 1000, p99: 2000, failRate: 0.03 },
  soak: { p95: 800, p99: 1500, failRate: 0.02 },
  spike: { p95: 1000, p99: 2000, failRate: 0.03 },
};

const threshold = profiles[profile] ?? profiles.default;
const errors = [];

if (p95 > threshold.p95) {
  errors.push(`p95 exceeded: ${p95} > ${threshold.p95}`);
}
if (p99 > threshold.p99) {
  errors.push(`p99 exceeded: ${p99} > ${threshold.p99}`);
}
if (failedRate > threshold.failRate) {
  errors.push(`fail rate exceeded: ${failedRate} > ${threshold.failRate}`);
}

if (errors.length > 0) {
  errors.forEach((item) => console.error(item));
  process.exit(1);
}

console.log(`Perf summary passed (${profile}): p95=${p95}, p99=${p99}, failRate=${failedRate}`);
