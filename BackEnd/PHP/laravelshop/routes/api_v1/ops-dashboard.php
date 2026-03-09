<?php

use App\Http\Controllers\Api\V1\OpsDashboard\OpsDashboardController;
use Illuminate\Support\Facades\Route;

Route::prefix('admin/ops-dashboard')->middleware('auth.api')->group(function (): void {
    Route::get('/summary', [OpsDashboardController::class, 'summary']);
});
