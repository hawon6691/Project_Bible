const fs = require('fs');
const path = require('path');

const cwd = process.cwd();
const outDir = path.join(cwd, 'test-results');
const run1Path = path.join(outDir, 'critical-run-1.json');
const run2Path = path.join(outDir, 'critical-run-2.json');
const reportPath = path.join(outDir, 'critical-stability-report.json');

function extractFailures(report) {
  const failures = [];
  for (const suite of report.testResults || []) {
    for (const assertion of suite.assertionResults || []) {
      if (assertion.status === 'failed') {
        failures.push(`${suite.name}::${assertion.fullName}`);
      }
    }
  }
  return failures.sort();
}

function readJson(filePath) {
  if (!fs.existsSync(filePath)) {
    throw new Error(`Missing file: ${filePath}`);
  }
  return JSON.parse(fs.readFileSync(filePath, 'utf8'));
}

const run1 = readJson(run1Path);
const run2 = readJson(run2Path);

const failed1 = extractFailures(run1);
const failed2 = extractFailures(run2);

const signature1 = JSON.stringify(failed1);
const signature2 = JSON.stringify(failed2);
const flakyDetected = signature1 !== signature2;

const report = {
  checkedAt: new Date().toISOString(),
  run1: {
    numFailedTests: run1.numFailedTests ?? 0,
    failedAssertions: failed1,
  },
  run2: {
    numFailedTests: run2.numFailedTests ?? 0,
    failedAssertions: failed2,
  },
  flakyDetected,
};

fs.mkdirSync(outDir, { recursive: true });
fs.writeFileSync(reportPath, JSON.stringify(report, null, 2), 'utf8');

if (flakyDetected) {
  console.error('Flaky pattern detected between critical-run-1 and critical-run-2.');
  process.exit(1);
}

console.log('Stability analysis passed: no flaky diff detected.');
