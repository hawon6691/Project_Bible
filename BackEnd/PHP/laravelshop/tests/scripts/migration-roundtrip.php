<?php

declare(strict_types=1);

$allow = strtolower((string) getenv('MIGRATION_ROUNDTRIP_ALLOW')) === 'true';
if (! $allow) {
    fwrite(STDERR, "MIGRATION_ROUNDTRIP_ALLOW=true is required for migration roundtrip test.\n");
    exit(1);
}

$root = dirname(__DIR__, 2);
$outDir = $root.DIRECTORY_SEPARATOR.'test-results';
$outFile = $outDir.DIRECTORY_SEPARATOR.'migration-roundtrip-report.json';

if (! is_dir($outDir)) {
    mkdir($outDir, 0777, true);
}

function runCommand(string $command): array
{
    $output = [];
    $code = 0;
    exec($command.' 2>&1', $output, $code);

    return [$code, implode(PHP_EOL, $output)];
}

$report = [
    'checkedAt' => gmdate(DATE_ATOM),
    'steps' => [],
];

[$code1, $output1] = runCommand('php artisan migrate:fresh --force');
$report['steps'][] = ['step' => 'migrate:fresh', 'ok' => $code1 === 0, 'output' => $output1];

[$code2, $output2] = runCommand('php artisan migrate:rollback --step=5 --force');
$report['steps'][] = ['step' => 'migrate:rollback', 'ok' => $code2 === 0, 'output' => $output2];

[$code3, $output3] = runCommand('php artisan migrate --force');
$report['steps'][] = ['step' => 'migrate', 'ok' => $code3 === 0, 'output' => $output3];

file_put_contents($outFile, json_encode($report, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES));

if ($code1 !== 0 || $code2 !== 0 || $code3 !== 0) {
    fwrite(STDERR, "Migration roundtrip failed.\n");
    exit(1);
}

fwrite(STDOUT, "Migration roundtrip passed.\n");
