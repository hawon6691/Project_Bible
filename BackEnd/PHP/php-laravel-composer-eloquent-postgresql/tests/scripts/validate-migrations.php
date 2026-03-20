<?php

declare(strict_types=1);

$root = dirname(__DIR__, 2);
$migrationsDir = $root.DIRECTORY_SEPARATOR.'database'.DIRECTORY_SEPARATOR.'migrations';
$outDir = $root.DIRECTORY_SEPARATOR.'test-results';
$outFile = $outDir.DIRECTORY_SEPARATOR.'migration-validation.json';

if (! is_dir($migrationsDir)) {
    fwrite(STDERR, "Migrations directory not found: {$migrationsDir}\n");
    exit(1);
}

$files = array_values(array_filter(scandir($migrationsDir) ?: [], static fn (string $name): bool => str_ends_with($name, '.php')));
sort($files);

$issues = [];
$timestamps = [];

if ($files === []) {
    $issues[] = 'No migration files found.';
}

foreach ($files as $file) {
    if (! preg_match('/^(\d{4}_\d{2}_\d{2}_\d{6})_.+\.php$/', $file, $matches)) {
        $issues[] = "Invalid migration filename format: {$file}";

        continue;
    }

    $timestamps[] = $matches[1];
    $fullPath = $migrationsDir.DIRECTORY_SEPARATOR.$file;
    $content = file_get_contents($fullPath) ?: '';

    if (! str_contains($content, 'extends Migration')) {
        $issues[] = "Migration does not extend Migration: {$file}";
    }
    if (! preg_match('/function\s+up\s*\(\)\s*:\s*void/', $content)) {
        $issues[] = "Missing up() method: {$file}";
    }
    if (! preg_match('/function\s+down\s*\(\)\s*:\s*void/', $content)) {
        $issues[] = "Missing down() method: {$file}";
    }
}

for ($i = 1, $count = count($timestamps); $i < $count; $i++) {
    if ($timestamps[$i] <= $timestamps[$i - 1]) {
        $issues[] = "Migration timestamps are not strictly increasing: {$timestamps[$i - 1]} -> {$timestamps[$i]}";
    }
}

if (! is_dir($outDir)) {
    mkdir($outDir, 0777, true);
}

$report = [
    'checkedAt' => gmdate(DATE_ATOM),
    'migrationsDir' => $migrationsDir,
    'files' => $files,
    'issueCount' => count($issues),
    'issues' => $issues,
];

file_put_contents($outFile, json_encode($report, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES));

if ($issues !== []) {
    foreach ($issues as $issue) {
        fwrite(STDERR, "- {$issue}\n");
    }
    exit(1);
}

fwrite(STDOUT, 'Migration validation passed. ('.count($files)." files)\n");
