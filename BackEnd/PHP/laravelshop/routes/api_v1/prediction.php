<?php

use App\Http\Controllers\Api\V1\Prediction\PredictionController;
use Illuminate\Support\Facades\Route;

Route::get('/predictions/products/{productId}/price-trend', [PredictionController::class, 'priceTrend'])->whereNumber('productId');
