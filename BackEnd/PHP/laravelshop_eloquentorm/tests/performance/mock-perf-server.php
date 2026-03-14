<?php

declare(strict_types=1);

$uri = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?: '/';
header('Content-Type: application/json');

$routes = [
    '/health' => ['status' => 'ok', 'service' => 'pbshop-php-perf'],
    '/api/v1/health' => ['status' => 'ok', 'service' => 'pbshop-php-perf'],
    '/api/v1/products' => [
        'items' => [
            ['id' => 1, 'name' => 'PBShop Mock Product A', 'minPrice' => 129000],
            ['id' => 2, 'name' => 'PBShop Mock Product B', 'minPrice' => 189000],
        ],
        'pagination' => ['page' => 1, 'limit' => 20, 'total' => 2, 'totalPages' => 1],
    ],
    '/api/v1/categories' => [
        'items' => [
            ['id' => 1, 'name' => '컴퓨터/노트북/조립PC'],
            ['id' => 2, 'name' => '가전/TV'],
        ],
    ],
    '/api/v1/rankings/products/popular' => [
        'items' => [
            ['productId' => 1, 'score' => 98.1],
            ['productId' => 2, 'score' => 91.2],
        ],
    ],
];

if (! array_key_exists($uri, $routes)) {
    http_response_code(404);
    echo json_encode(['message' => 'Not Found', 'path' => $uri], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);

    return;
}

echo json_encode($routes[$uri], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
