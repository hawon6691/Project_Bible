<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class SetApiLocale
{
    public function handle(Request $request, Closure $next): Response
    {
        $locale = $request->header('Accept-Language', config('app.locale', 'ko'));
        $normalized = str($locale)->before(',')->before('-')->lower()->value();

        if (! in_array($normalized, ['ko', 'en', 'ja'], true)) {
            $normalized = config('app.fallback_locale', 'ko');
        }

        app()->setLocale($normalized);

        return $next($request);
    }
}
