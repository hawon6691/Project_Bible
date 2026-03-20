<?php

namespace App\Modules\UsedMarket\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Category;
use App\Models\PcBuild;
use App\Models\PriceEntry;
use App\Models\Product;
use App\Models\UsedMarketPrice;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class UsedMarketService
{
    public function productPrice(int $productId): array
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $prices = UsedMarketPrice::query()->where('product_id', $productId)->pluck('price');
        if ($prices->isEmpty()) {
            $fallback = PriceEntry::query()->where('product_id', $productId)->pluck('price')->map(fn ($price) => round($price * 0.65, 2));
            $prices = $fallback;
        }

        return [
            'averagePrice' => round($prices->avg() ?? 0, 2),
            'minPrice' => $prices->min(),
            'maxPrice' => $prices->max(),
            'trend' => 'STABLE',
        ];
    }

    public function categoryPrices(int $categoryId, int $page, int $limit): array
    {
        $category = Category::query()->find($categoryId);
        if (! $category) {
            throw new BusinessException('카테고리를 찾을 수 없습니다.', 'CATEGORY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $result = Product::query()->where('category_id', $categoryId)->orderBy('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (Product $product): array => array_merge([
                'productId' => $product->id,
                'productName' => $product->name,
            ], $this->productPrice($product->id)))->all(),
            'pagination' => [
                'page' => $result->currentPage(),
                'limit' => $result->perPage(),
                'total' => $result->total(),
                'totalPages' => $result->lastPage(),
            ],
        ];
    }

    public function estimateBuild(User $user, int $buildId): array
    {
        $build = PcBuild::query()->with('parts.product')->where('user_id', $user->id)->find($buildId);
        if (! $build) {
            throw new BusinessException('견적을 찾을 수 없습니다.', 'PC_BUILD_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $items = $build->parts->map(function ($part): array {
            $price = PriceEntry::query()->where('product_id', $part->product_id)->orderBy('price')->first();
            $estimated = $price ? round($price->price * 0.55 * $part->quantity, 2) : 0;

            return [
                'partId' => $part->id,
                'productId' => $part->product_id,
                'productName' => $part->product?->name,
                'estimatedPrice' => $estimated,
            ];
        })->all();

        return [
            'estimatedPrice' => collect($items)->sum('estimatedPrice'),
            'partBreakdown' => $items,
        ];
    }
}
