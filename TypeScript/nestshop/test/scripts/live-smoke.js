const fs = require('fs');
const path = require('path');

const base = (process.env.LIVE_SMOKE_BASE_URL || '').trim();
if (!base) {
  console.error('LIVE_SMOKE_BASE_URL is required. e.g. https://api.example.com');
  process.exit(1);
}

const prefix = (process.env.LIVE_SMOKE_PREFIX || '/api/v1').trim();
const authHeader = (process.env.LIVE_SMOKE_AUTH || '').trim();

const outDir = path.join(process.cwd(), 'test-results');
const outFile = path.join(outDir, 'live-smoke-report.json');

function buildUrl(route) {
  return `${base.replace(/\/$/, '')}${prefix.startsWith('/') ? prefix : `/${prefix}`}${route}`;
}

async function hit(name, route, method = 'GET', allowStatus = [200]) {
  const url = buildUrl(route);
  const headers = {};
  if (authHeader) {
    headers.Authorization = authHeader;
  }

  const response = await fetch(url, { method, headers });
  let body = null;
  try {
    body = await response.json();
  } catch {
    body = null;
  }

  const ok = allowStatus.includes(response.status);
  return {
    name,
    url,
    method,
    status: response.status,
    ok,
    allowStatus,
    bodyPreview: body,
  };
}

async function main() {
  const checks = [];
  checks.push(await hit('health', '/health', 'GET', [200]));
  checks.push(await hit('search', '/search?keyword=rtx&page=1&limit=5', 'GET', [200]));
  checks.push(await hit('ops-dashboard', '/admin/ops-dashboard/summary', 'GET', [200, 401, 403]));
  checks.push(await hit('queue-stats', '/admin/queues/stats', 'GET', [200, 401, 403]));

  const failed = checks.filter((item) => !item.ok);
  const report = {
    checkedAt: new Date().toISOString(),
    base,
    prefix,
    total: checks.length,
    failed: failed.length,
    checks,
  };

  fs.mkdirSync(outDir, { recursive: true });
  fs.writeFileSync(outFile, JSON.stringify(report, null, 2), 'utf8');

  if (failed.length > 0) {
    console.error(`Live smoke failed: ${failed.length}/${checks.length}`);
    failed.forEach((f) => console.error(`- ${f.name}: ${f.status} (${f.url})`));
    process.exit(1);
  }

  console.log(`Live smoke passed: ${checks.length}/${checks.length}`);
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : String(error));
  process.exit(1);
});
