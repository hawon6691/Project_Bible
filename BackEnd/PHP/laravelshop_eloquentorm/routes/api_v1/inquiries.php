<?php

use App\Http\Controllers\Api\V1\Inquiry\InquiryController;
use Illuminate\Support\Facades\Route;

Route::get('/products/{productId}/inquiries', [InquiryController::class, 'index'])->whereNumber('productId');

Route::middleware('auth.api')->group(function (): void {
    Route::post('/products/{productId}/inquiries', [InquiryController::class, 'store'])->whereNumber('productId');
    Route::post('/inquiries/{id}/answer', [InquiryController::class, 'answer'])->whereNumber('id');
    Route::get('/inquiries/me', [InquiryController::class, 'mine']);
    Route::delete('/inquiries/{id}', [InquiryController::class, 'destroy'])->whereNumber('id');
});
