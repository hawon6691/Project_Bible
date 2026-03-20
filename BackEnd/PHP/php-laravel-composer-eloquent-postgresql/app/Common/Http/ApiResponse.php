<?php

namespace App\Common\Http;

use Illuminate\Http\JsonResponse;

final class ApiResponse
{
    public static function success(mixed $data = null, array $meta = [], int $status = 200): JsonResponse
    {
        $meta = self::withContextMeta($meta);

        $payload = [
            'success' => true,
            'data' => $data,
        ];

        if ($meta !== []) {
            $payload['meta'] = $meta;
        }

        return response()->json($payload, $status);
    }

    public static function error(string $message, string $code = 'INTERNAL_SERVER_ERROR', int $status = 500, array $details = []): JsonResponse
    {
        $meta = self::withContextMeta();

        $payload = [
            'success' => false,
            'error' => [
                'code' => $code,
                'message' => $message,
            ],
        ];

        if ($details !== []) {
            $payload['error']['details'] = $details;
        }

        if ($meta !== []) {
            $payload['meta'] = $meta;
        }

        return response()->json($payload, $status);
    }

    private static function withContextMeta(array $meta = []): array
    {
        $request = request();

        $contextMeta = array_filter([
            'requestId' => $request?->attributes->get('request_id'),
            'locale' => app()->getLocale(),
            'currency' => $request?->attributes->get('currency'),
        ], static fn ($value): bool => filled($value));

        return array_merge($contextMeta, $meta);
    }
}
