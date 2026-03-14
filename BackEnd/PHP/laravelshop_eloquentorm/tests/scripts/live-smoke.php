<?php

declare(strict_types=1);

$base = rtrim(trim((string) getenv('LIVE_SMOKE_BASE_URL')), '/');
if ($base === '') {
    fwrite(STDERR, "LIVE_SMOKE_BASE_URL is required.\n");
    exit(1);
}

$prefix = trim((string) getenv('LIVE_SMOKE_PREFIX'));
$prefix = $prefix !== '' ? $prefix : '/api/v1';
$prefix = str_starts_with($prefix, '/') ? $prefix : '/'.$prefix;
$authHeader = trim((string) getenv('LIVE_SMOKE_AUTH'));

$root = dirname(__DIR__, 2);
$outDir = $root.DIRECTORY_SEPARATOR.'test-results';
$outFile = $outDir.DIRECTORY_SEPARATOR.'live-smoke-report.json';

if (! is_dir($outDir)) {
    mkdir($outDir, 0777, true);
}

function request(string $url, string $authHeader = ''): array
{
    $headers = ['Accept: application/json'];
    if ($authHeader !== '') {
        $headers[] = 'Authorization: '.$authHeader;
    }

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => $headers,
        CURLOPT_TIMEOUT => 15,
        CURLOPT_HEADER => false,
    ]);

    $body = curl_exec($ch);
    $status = (int) curl_getinfo($ch, CURLINFO_RESPONSE_CODE);
    $error = curl_error($ch);
    curl_close($ch);

    return [$status, $body === false ? null : $body, $error];
}

$checks = [];
$targets = [
    ['name' => 'health', 'route' => '/health', 'allow' => [200]],
    ['name' => 'categories', 'route' => '/categories', 'allow' => [200]],
    ['name' => 'products', 'route' => '/products', 'allow' => [200]],
    ['name' => 'ops-dashboard', 'route' => '/admin/ops-dashboard/summary', 'allow' => [200, 401, 403]],
];

foreach ($targets as $target) {
    $url = $base.$prefix.$target['route'];
    [$status, $body, $error] = request($url, $authHeader);
    $checks[] = [
        'name' => $target['name'],
        'url' => $url,
        'status' => $status,
        'ok' => in_array($status, $target['allow'], true),
        'allowStatus' => $target['allow'],
        'bodyPreview' => $body !== null ? mb_substr($body, 0, 500) : null,
        'error' => $error !== '' ? $error : null,
    ];
}

$failed = array_values(array_filter($checks, static fn (array $item): bool => $item['ok'] !== true));
$report = [
    'checkedAt' => gmdate(DATE_ATOM),
    'base' => $base,
    'prefix' => $prefix,
    'total' => count($checks),
    'failed' => count($failed),
    'checks' => $checks,
];

file_put_contents($outFile, json_encode($report, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES));

if ($failed !== []) {
    fwrite(STDERR, 'Live smoke failed: '.count($failed).'/'.count($checks).PHP_EOL);
    exit(1);
}

fwrite(STDOUT, 'Live smoke passed: '.count($checks).'/'.count($checks).PHP_EOL);
