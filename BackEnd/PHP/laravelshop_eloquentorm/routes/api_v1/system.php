<?php

use App\Http\Controllers\Api\V1\System\SystemController;
use Illuminate\Support\Facades\Route;

Route::get('/health', [SystemController::class, 'health']);
