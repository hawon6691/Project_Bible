<?php

namespace App\Modules\Analytics\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\PriceEntry;
use App\Models\Product;
use Symfony\Component\HttpFoundation\Response;

class AnalyticsService
{
    public function lowestEver(int $productId): array
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $entries = PriceEntry::query()->where('product_id', $productId)->orderBy('price')->get();
        $current = $entries->last();
        $lowest = $entries->first();

        return [
            'isLowestEver' => $current && $lowest ? (float) $current->price <= (float) $lowest->price : false,
            'currentPrice' => $current ? (float) $current->price : null,
            'lowestPrice' => $lowest ? (float) $lowest->price : null,
            'lowestDate' => optional($lowest?->collected_at)?->toISOString(),
        ];
    }

    public function unitPrice(int $productId): array
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $entry = PriceEntry::query()->where('product_id', $productId)->orderBy('price')->first();
        $quantity = 1;
        $unit = 'ea';
        $unitPrice = $entry ? round((float) $entry->price / $quantity, 2) : null;

        return [
            'unitPrice' => $unitPrice,
            'unit' => $unit,
            'quantity' => $quantity,
        ];
    }
}
