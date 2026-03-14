<?php

declare(strict_types=1);

[$script, $summaryPath, $profile] = array_pad($argv, 3, null);
$profile = $profile ?? 'default';

if ($summaryPath === null) {
    fwrite(STDERR, "Usage: php tests/performance/assert-summary.php <summaryPath> [profile]\n");
    exit(1);
}

if (! file_exists($summaryPath)) {
    fwrite(STDERR, "Summary file not found: {$summaryPath}\n");
    exit(1);
}

$summary = json_decode((string) file_get_contents($summaryPath), true, 512, JSON_THROW_ON_ERROR);
$durationValues = $summary['metrics']['http_req_duration']['values'] ?? [];
$failedValues = $summary['metrics']['http_req_failed']['values'] ?? [];

$p95 = (float) ($durationValues['p(95)'] ?? INF);
$p99 = (float) ($durationValues['p(99)'] ?? INF);
$failedRate = (float) ($failedValues['rate'] ?? INF);

$profiles = [
    'default' => ['p95' => 1000.0, 'p99' => 2000.0, 'failRate' => 0.03],
    'soak' => ['p95' => 800.0, 'p99' => 1500.0, 'failRate' => 0.02],
    'spike' => ['p95' => 1000.0, 'p99' => 2000.0, 'failRate' => 0.03],
];

$threshold = $profiles[$profile] ?? $profiles['default'];
$errors = [];

if ($p95 > $threshold['p95']) {
    $errors[] = "p95 exceeded: {$p95} > {$threshold['p95']}";
}
if ($p99 > $threshold['p99']) {
    $errors[] = "p99 exceeded: {$p99} > {$threshold['p99']}";
}
if ($failedRate > $threshold['failRate']) {
    $errors[] = "fail rate exceeded: {$failedRate} > {$threshold['failRate']}";
}

if ($errors !== []) {
    foreach ($errors as $error) {
        fwrite(STDERR, $error.PHP_EOL);
    }
    exit(1);
}

fwrite(STDOUT, "Perf summary passed ({$profile}): p95={$p95}, p99={$p99}, failRate={$failedRate}\n");
