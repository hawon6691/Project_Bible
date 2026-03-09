<?php

namespace App\Modules\Compare\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\CompareItem;
use App\Models\Product;
use Symfony\Component\HttpFoundation\Response;

class CompareService
{
    public function add(string $compareKey, int $productId): array
    {
        if (! Product::query()->where('id', $productId)->exists()) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        if (CompareItem::query()->where('compare_key', $compareKey)->count() >= 4 && ! CompareItem::query()->where('compare_key', $compareKey)->where('product_id', $productId)->exists()) {
            throw new BusinessException('비교함에는 최대 4개까지 담을 수 있습니다.', 'COMPARE_LIMIT_EXCEEDED', Response::HTTP_BAD_REQUEST);
        }

        CompareItem::query()->firstOrCreate([
            'compare_key' => $compareKey,
            'product_id' => $productId,
        ]);

        return $this->list($compareKey);
    }

    public function remove(string $compareKey, int $productId): array
    {
        CompareItem::query()->where('compare_key', $compareKey)->where('product_id', $productId)->delete();

        return $this->list($compareKey);
    }

    public function list(string $compareKey): array
    {
        $items = CompareItem::query()->where('compare_key', $compareKey)->orderBy('id')->get()->map(function (CompareItem $item): array {
            $product = Product::query()->find($item->product_id);
            return [
                'productId' => $item->product_id,
                'name' => $product?->name,
                'slug' => $product?->slug,
            ];
        })->all();

        return ['compareList' => $items];
    }

    public function detail(string $compareKey): array
    {
        $items = CompareItem::query()->where('compare_key', $compareKey)->orderBy('id')->get();
        $products = Product::query()->whereIn('id', $items->pluck('product_id'))->with('specs')->get();

        return [
            'items' => $products->map(fn (Product $product): array => [
                'productId' => $product->id,
                'name' => $product->name,
                'specs' => $product->specs->map(fn ($spec): array => [
                    'name' => $spec->name,
                    'value' => $spec->value,
                ])->all(),
            ])->all(),
        ];
    }
}
