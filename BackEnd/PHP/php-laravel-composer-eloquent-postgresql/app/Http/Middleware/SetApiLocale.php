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
        $supportedLocales = config('pbshop.i18n.supported_locales', ['ko', 'en', 'ja']);

        if (! in_array($normalized, $supportedLocales, true)) {
            $normalized = config('app.fallback_locale', 'ko');
        }

        app()->setLocale($normalized);

        /** @var Response $response */
        $response = $next($request);
        $response->headers->set('Content-Language', $normalized);

        return $response;
    }
}
