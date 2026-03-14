<?php

namespace App\Modules\Ranking\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Product;
use App\Models\RecentProductView;
use App\Models\SearchHistory;
use App\Models\User;
use Illuminate\Support\Facades\DB;
use Symfony\Component\HttpFoundation\Response;

class RankingService
{
    public function popularProducts(?int $categoryId = null, int $limit = 20): array
    {
        $limit = min(100, max(1, $limit));
        $query = RecentProductView::query()
            ->select('product_id', DB::raw('COUNT(*) as score'))
            ->groupBy('product_id')
            ->orderByDesc('score')
            ->limit($limit);

        if ($categoryId) {
            $query->whereHas('product', fn ($builder) => $builder->where('category_id', $categoryId));
        }

        return $query->get()->values()->map(function ($row, int $index): array {
            $product = Product::query()->with('priceEntries')->find($row->product_id);

            return [
                'rank' => $index + 1,
                'rankChange' => 0,
                'product' => $product ? [
                    'id' => $product->id,
                    'name' => $product->name,
                    'lowestPrice' => (float) ($product->priceEntries->min('price') ?? 0),
                    'thumbnailUrl' => $product->thumbnail_url,
                ] : null,
                'score' => (int) $row->score,
            ];
        })->all();
    }

    public function popularKeywords(int $limit = 20): array
    {
        $limit = min(100, max(1, $limit));

        return SearchHistory::query()
            ->select('keyword', DB::raw('COUNT(*) as search_count'))
            ->groupBy('keyword')
            ->orderByDesc('search_count')
            ->limit($limit)
            ->get()
            ->values()
            ->map(fn ($row, int $index): array => [
                'rank' => $index + 1,
                'keyword' => $row->keyword,
                'searchCount' => (int) $row->search_count,
                'rankChange' => 0,
            ])->all();
    }

    public function recalculate(User $actor): array
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $updatedCount = RecentProductView::query()->distinct('product_id')->count('product_id');

        return [
            'updatedCount' => $updatedCount,
        ];
    }
}
