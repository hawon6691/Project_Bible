const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const { Client } = require('pg');

const allow = String(process.env.MIGRATION_ROUNDTRIP_ALLOW || '').toLowerCase() === 'true';
if (!allow) {
  console.error('MIGRATION_ROUNDTRIP_ALLOW=true is required for migration roundtrip test.');
  process.exit(1);
}

const outDir = path.join(process.cwd(), 'test-results');
const outFile = path.join(outDir, 'migration-roundtrip-report.json');

const dbConfig = {
  host: process.env.DB_HOST ?? 'localhost',
  port: Number(process.env.DB_PORT ?? 5432),
  user: process.env.DB_USERNAME ?? 'postgres',
  password: process.env.DB_PASSWORD ?? 'postgres',
  database: process.env.DB_DATABASE ?? 'nestshop',
};

async function getMigrationCount(client) {
  const result = await client.query('SELECT COUNT(*)::int AS count FROM migrations');
  return Number(result.rows[0]?.count ?? 0);
}

async function ensureMigrationsTable(client) {
  await client.query(`
    CREATE TABLE IF NOT EXISTS migrations (
      id SERIAL PRIMARY KEY,
      timestamp bigint NOT NULL,
      name varchar NOT NULL
    )
  `);
}

async function main() {
  const client = new Client(dbConfig);
  await client.connect();
  await ensureMigrationsTable(client);

  const report = {
    checkedAt: new Date().toISOString(),
    db: {
      host: dbConfig.host,
      port: dbConfig.port,
      database: dbConfig.database,
    },
    steps: [],
  };

  try {
    const initial = await getMigrationCount(client);
    report.initialCount = initial;

    execSync('npm run migration:run', { stdio: 'inherit' });
    const afterRun = await getMigrationCount(client);
    report.afterRunCount = afterRun;
    report.steps.push({ step: 'migration:run', ok: afterRun >= initial });

    let current = afterRun;
    while (current > initial) {
      execSync('npm run migration:revert', { stdio: 'inherit' });
      current = await getMigrationCount(client);
    }
    report.afterRevertCount = current;
    report.steps.push({ step: 'migration:revert(back to initial)', ok: current === initial });

    if (afterRun < initial) {
      throw new Error(`afterRunCount(${afterRun}) is smaller than initialCount(${initial})`);
    }
    if (current !== initial) {
      throw new Error(`afterRevertCount(${current}) did not return to initialCount(${initial})`);
    }
  } finally {
    fs.mkdirSync(outDir, { recursive: true });
    fs.writeFileSync(outFile, JSON.stringify(report, null, 2), 'utf8');
    await client.end();
  }
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : String(error));
  process.exit(1);
});
