<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class SetApiRequestContext
{
    public function handle(Request $request, Closure $next): Response
    {
        $requestId = $request->header('X-Request-Id', (string) Str::uuid());
        $supportedCurrencies = config('pbshop.i18n.supported_currencies', ['KRW', 'USD', 'JPY']);
        $currency = strtoupper((string) $request->header('X-Currency', $supportedCurrencies[0] ?? 'KRW'));

        if (! in_array($currency, $supportedCurrencies, true)) {
            $currency = $supportedCurrencies[0] ?? 'KRW';
        }

        $request->attributes->set('request_id', $requestId);
        $request->attributes->set('currency', $currency);

        /** @var Response $response */
        $response = $next($request);
        $response->headers->set('X-Request-Id', $requestId);
        $response->headers->set('X-Currency', $currency);

        return $response;
    }
}
