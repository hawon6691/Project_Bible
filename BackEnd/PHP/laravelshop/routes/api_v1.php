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
        require __DIR__.'/api_v1/prediction.php';
        require __DIR__.'/api_v1/deals.php';
        require __DIR__.'/api_v1/recommendations.php';
        require __DIR__.'/api_v1/rankings.php';
        require __DIR__.'/api_v1/fraud.php';
        require __DIR__.'/api_v1/trust.php';
        require __DIR__.'/api_v1/i18n.php';
        require __DIR__.'/api_v1/images.php';
        require __DIR__.'/api_v1/badges.php';
        require __DIR__.'/api_v1/pc-builds.php';
        require __DIR__.'/api_v1/friends.php';
        require __DIR__.'/api_v1/shortforms.php';
        require __DIR__.'/api_v1/media.php';
        require __DIR__.'/api_v1/news.php';
        require __DIR__.'/api_v1/matching.php';
        require __DIR__.'/api_v1/analytics.php';
        require __DIR__.'/api_v1/used-market.php';
        require __DIR__.'/api_v1/auto.php';
        require __DIR__.'/api_v1/auctions.php';
        require __DIR__.'/api_v1/compare.php';
        require __DIR__.'/api_v1/admin-settings.php';
        require __DIR__.'/api_v1/resilience.php';
        require __DIR__.'/api_v1/error-codes.php';
        require __DIR__.'/api_v1/queue-admin.php';
        require __DIR__.'/api_v1/ops-dashboard.php';
        require __DIR__.'/api_v1/observability.php';
        require __DIR__.'/api_v1/query.php';
        require __DIR__.'/api_v1/search-sync.php';
        require __DIR__.'/api_v1/crawler.php';
    });



