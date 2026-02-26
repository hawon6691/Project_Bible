const fs = require('fs');
const path = require('path');

const cwd = process.cwd();
const migrationsDir = path.join(cwd, 'src', 'database', 'migrations');
const outDir = path.join(cwd, 'test-results');
const outFile = path.join(outDir, 'migration-validation.json');

function fail(message) {
  console.error(message);
  process.exitCode = 1;
}

if (!fs.existsSync(migrationsDir)) {
  fail(`Migrations directory not found: ${migrationsDir}`);
}

const files = fs
  .readdirSync(migrationsDir)
  .filter((name) => name.endsWith('.ts') || name.endsWith('.js'))
  .sort((a, b) => a.localeCompare(b));

const issues = [];

if (files.length === 0) {
  issues.push('No migration files found.');
}

const timestamps = [];
for (const file of files) {
  const match = file.match(/^(\d+)-.+\.(ts|js)$/);
  if (!match) {
    issues.push(`Invalid migration filename format: ${file}`);
    continue;
  }

  const tsNum = Number(match[1]);
  if (!Number.isInteger(tsNum)) {
    issues.push(`Invalid migration timestamp: ${file}`);
  }
  timestamps.push(tsNum);

  const fullPath = path.join(migrationsDir, file);
  const content = fs.readFileSync(fullPath, 'utf8');
  if (!/implements\s+MigrationInterface/.test(content)) {
    issues.push(`MigrationInterface not implemented: ${file}`);
  }
  if (!/async\s+up\s*\(/.test(content)) {
    issues.push(`Missing up() method: ${file}`);
  }
  if (!/async\s+down\s*\(/.test(content)) {
    issues.push(`Missing down() method: ${file}`);
  }
}

for (let i = 1; i < timestamps.length; i += 1) {
  if (timestamps[i] <= timestamps[i - 1]) {
    issues.push(`Migration timestamps are not strictly increasing: ${timestamps[i - 1]} -> ${timestamps[i]}`);
  }
}

fs.mkdirSync(outDir, { recursive: true });
const report = {
  checkedAt: new Date().toISOString(),
  migrationsDir,
  files,
  issueCount: issues.length,
  issues,
};
fs.writeFileSync(outFile, JSON.stringify(report, null, 2), 'utf8');

if (issues.length > 0) {
  issues.forEach((issue) => console.error(`- ${issue}`));
  process.exit(1);
}

console.log(`Migration validation passed. (${files.length} files)`);
