<?php

declare(strict_types=1);

$root = dirname(__DIR__, 2);
$outDir = $root.DIRECTORY_SEPARATOR.'test-results';
$run1Path = $outDir.DIRECTORY_SEPARATOR.'critical-run-1.xml';
$run2Path = $outDir.DIRECTORY_SEPARATOR.'critical-run-2.xml';
$reportPath = $outDir.DIRECTORY_SEPARATOR.'critical-stability-report.json';

function extractFailures(string $filePath): array
{
    if (! file_exists($filePath)) {
        throw new RuntimeException("Missing file: {$filePath}");
    }

    $xml = simplexml_load_file($filePath);
    if ($xml === false) {
        throw new RuntimeException("Failed to parse XML: {$filePath}");
    }

    $failures = [];
    foreach ($xml->testsuite as $suite) {
        foreach ($suite->testcase as $case) {
            if (isset($case->failure) || isset($case->error)) {
                $suiteName = (string) ($suite['name'] ?? 'unknown-suite');
                $caseName = (string) ($case['name'] ?? 'unknown-case');
                $failures[] = $suiteName.'::'.$caseName;
            }
        }
    }

    sort($failures);

    return $failures;
}

$failed1 = extractFailures($run1Path);
$failed2 = extractFailures($run2Path);
$flakyDetected = json_encode($failed1) !== json_encode($failed2);

$report = [
    'checkedAt' => gmdate(DATE_ATOM),
    'run1' => ['failedAssertions' => $failed1],
    'run2' => ['failedAssertions' => $failed2],
    'flakyDetected' => $flakyDetected,
];

file_put_contents($reportPath, json_encode($report, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES));

if ($flakyDetected) {
    fwrite(STDERR, "Flaky pattern detected between critical-run-1 and critical-run-2.\n");
    exit(1);
}

fwrite(STDOUT, "Stability analysis passed: no flaky diff detected.\n");
