<?php

use App\Common\Constants\ApiRoutes;
use Illuminate\Support\Facades\Route;

Route::prefix(ApiRoutes::API_PREFIX)
    ->middleware(['api', 'api.context', 'api.locale'])
    ->group(function (): void {
        require __DIR__.'/api_v1/system.php';
        require __DIR__.'/api_v1/auth.php';
        require __DIR__.'/api_v1/users.php';
        require __DIR__.'/api_v1/categories.php';
        require __DIR__.'/api_v1/products.php';
        require __DIR__.'/api_v1/specs.php';
        require __DIR__.'/api_v1/sellers.php';
        require __DIR__.'/api_v1/prices.php';
        require __DIR__.'/api_v1/cart.php';
        require __DIR__.'/api_v1/addresses.php';
        require __DIR__.'/api_v1/orders.php';
        require __DIR__.'/api_v1/payments.php';
        require __DIR__.'/api_v1/reviews.php';
        require __DIR__.'/api_v1/wishlist.php';
        require __DIR__.'/api_v1/points.php';
        require __DIR__.'/api_v1/community.php';
        require __DIR__.'/api_v1/inquiries.php';
        require __DIR__.'/api_v1/support.php';
        require __DIR__.'/api_v1/activity.php';
        require __DIR__.'/api_v1/chat.php';
        require __DIR__.'/api_v1/push.php';
    });
