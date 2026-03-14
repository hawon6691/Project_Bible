const fs = require('fs');

const [summaryPath, scenario = 'generic'] = process.argv.slice(2);

if (!summaryPath) {
  console.error('summary path is required');
  process.exit(1);
}

const summary = JSON.parse(fs.readFileSync(summaryPath, 'utf8'));
const passed = (summary.metrics.http_req_failed?.values?.rate || 0) === 0;

if (!passed) {
  console.error(`${scenario} performance summary contains failed requests`);
  process.exit(1);
}

console.log(`${scenario} performance summary looks healthy`);
