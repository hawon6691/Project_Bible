<?php

namespace App\Modules\Activity\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Product;
use App\Models\RecentProductView;
use App\Models\SearchHistory;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class ActivityService
{
    public function getSummary(User $user): array
    {
        return [
            'recentProductCount' => RecentProductView::query()->where('user_id', $user->id)->count(),
            'searchCount' => SearchHistory::query()->where('user_id', $user->id)->count(),
            'recentProducts' => $this->getRecentProducts($user, 1, 5)['items'],
            'searches' => $this->getSearchHistory($user, 1, 5)['items'],
        ];
    }

    public function getRecentProducts(User $user, int $page = 1, int $limit = 20): array
    {
        $query = RecentProductView::query()
            ->with('product')
            ->where('user_id', $user->id)
            ->orderByDesc('viewed_at');

        return [
            'items' => $query->forPage($page, $limit)->get()
                ->map(fn (RecentProductView $view): array => [
                    'id' => $view->id,
                    'productId' => $view->product_id,
                    'productName' => $view->product?->name,
                    'viewedAt' => optional($view->viewed_at)?->toISOString(),
                ])->values()->all(),
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => $query->count(),
            ],
        ];
    }

    public function addRecentProduct(User $user, int $productId): array
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $view = RecentProductView::query()->updateOrCreate(
            ['user_id' => $user->id, 'product_id' => $productId],
            ['viewed_at' => now()],
        );

        return [
            'id' => $view->id,
            'productId' => $view->product_id,
            'productName' => $product->name,
            'viewedAt' => optional($view->viewed_at)?->toISOString(),
        ];
    }

    public function getSearchHistory(User $user, int $page = 1, int $limit = 20): array
    {
        $query = SearchHistory::query()
            ->where('user_id', $user->id)
            ->orderByDesc('searched_at');

        return [
            'items' => $query->forPage($page, $limit)->get()
                ->map(fn (SearchHistory $history): array => [
                    'id' => $history->id,
                    'keyword' => $history->keyword,
                    'searchedAt' => optional($history->searched_at)?->toISOString(),
                ])->values()->all(),
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => $query->count(),
            ],
        ];
    }

    public function addSearchHistory(User $user, array $payload): array
    {
        $history = SearchHistory::query()->create([
            'user_id' => $user->id,
            'keyword' => $payload['keyword'],
            'searched_at' => now(),
        ]);

        return [
            'id' => $history->id,
            'keyword' => $history->keyword,
            'searchedAt' => optional($history->searched_at)?->toISOString(),
        ];
    }

    public function removeSearchHistory(User $user, int $historyId): array
    {
        $history = SearchHistory::query()->where('user_id', $user->id)->find($historyId);
        if (! $history) {
            throw new BusinessException('검색 기록을 찾을 수 없습니다.', 'SEARCH_HISTORY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $history->delete();

        return ['message' => '검색 기록이 삭제되었습니다.'];
    }

    public function clearSearchHistory(User $user): array
    {
        SearchHistory::query()->where('user_id', $user->id)->delete();

        return ['message' => '검색 기록이 전체 삭제되었습니다.'];
    }
}
