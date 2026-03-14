<?php

use App\Http\Controllers\Api\V1\I18n\I18nController;
use Illuminate\Support\Facades\Route;

Route::get('/i18n/translations', [I18nController::class, 'translations']);
Route::get('/i18n/exchange-rates', [I18nController::class, 'exchangeRates']);
Route::get('/i18n/convert', [I18nController::class, 'convert']);

Route::middleware('auth.api')->group(function (): void {
    Route::post('/i18n/admin/translations', [I18nController::class, 'upsertTranslation']);
    Route::delete('/i18n/admin/translations/{id}', [I18nController::class, 'deleteTranslation'])->whereNumber('id');
    Route::post('/i18n/admin/exchange-rates', [I18nController::class, 'upsertExchangeRate']);
});
