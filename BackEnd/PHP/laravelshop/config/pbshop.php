<?php

return [
    'project' => [
        'name' => env('APP_NAME', 'PBShop'),
        'reference_implementation' => 'TypeScript + NestJS + PostgreSQL',
        'language' => 'php',
        'framework' => 'laravel',
    ],

    'api' => [
        'prefix' => env('API_PREFIX', 'v1'),
        'base_path' => '/api/'.env('API_PREFIX', 'v1'),
        'frontend_url' => env('FRONTEND_URL', 'http://localhost:3001'),
        'response_contract' => [
            'success' => true,
            'data' => [],
            'meta' => [],
            'error' => null,
        ],
    ],

    'i18n' => [
        'default_locale' => env('APP_LOCALE', 'ko'),
        'fallback_locale' => env('APP_FALLBACK_LOCALE', 'ko'),
        'supported_locales' => array_values(array_filter(array_map(
            static fn (string $value): string => trim($value),
            explode(',', (string) env('APP_SUPPORTED_LOCALES', 'ko,en,ja'))
        ))),
        'supported_currencies' => array_values(array_filter(array_map(
            static fn (string $value): string => trim($value),
            explode(',', (string) env('APP_SUPPORTED_CURRENCIES', 'KRW,USD,JPY'))
        ))),
    ],

    'infrastructure' => [
        'database' => env('DB_CONNECTION', 'mysql'),
        'cache_store' => env('CACHE_STORE', 'database'),
        'queue_connection' => env('QUEUE_CONNECTION', 'database'),
        'session_driver' => env('SESSION_DRIVER', 'database'),
    ],
];
