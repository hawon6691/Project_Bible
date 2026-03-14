<?php

namespace App\Modules\Price\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\PriceAlert;
use App\Models\PriceEntry;
use App\Models\Product;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class PriceService
{
    public function listProductPrices(int $productId): array
    {
        $product = Product::query()->with('priceEntries.seller')->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $entries = $product->priceEntries;
        $prices = $entries->pluck('price')->map(fn ($value) => (float) $value);

        return [
            'productId' => $product->id,
            'productName' => $product->name,
            'lowestPrice' => $prices->min(),
            'averagePrice' => $prices->isNotEmpty() ? round((float) $prices->avg(), 2) : null,
            'highestPrice' => $prices->max(),
            'entries' => $entries->map(fn (PriceEntry $entry): array => $this->serializePriceEntry($entry))->values()->all(),
        ];
    }

    public function createPrice(User $actor, int $productId, array $payload): array
    {
        $this->assertSellerOrAdmin($actor);

        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $entry = PriceEntry::query()->create([
            'product_id' => $productId,
            'seller_id' => $payload['sellerId'],
            'price' => $payload['price'],
            'shipping_fee' => $payload['shippingFee'] ?? 0,
            'stock_status' => $payload['stockStatus'] ?? 'IN_STOCK',
            'is_card_discount' => (bool) ($payload['isCardDiscount'] ?? false),
            'is_cash_discount' => (bool) ($payload['isCashDiscount'] ?? false),
            'collected_at' => now(),
        ]);

        return $this->serializePriceEntry($entry->fresh('seller'));
    }

    public function updatePrice(User $actor, int $priceId, array $payload): array
    {
        $this->assertSellerOrAdmin($actor);

        $entry = PriceEntry::query()->find($priceId);
        if (! $entry) {
            throw new BusinessException('가격 정보를 찾을 수 없습니다.', 'PRICE_ENTRY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $updates = [];
        if (array_key_exists('price', $payload)) {
            $updates['price'] = $payload['price'];
        }
        if (array_key_exists('shippingFee', $payload)) {
            $updates['shipping_fee'] = $payload['shippingFee'];
        }
        if (array_key_exists('stockStatus', $payload)) {
            $updates['stock_status'] = $payload['stockStatus'];
        }
        if (array_key_exists('isCardDiscount', $payload)) {
            $updates['is_card_discount'] = (bool) $payload['isCardDiscount'];
        }
        if (array_key_exists('isCashDiscount', $payload)) {
            $updates['is_cash_discount'] = (bool) $payload['isCashDiscount'];
        }
        if ($updates !== []) {
            $updates['collected_at'] = now();
            $entry->forceFill($updates)->save();
        }

        return $this->serializePriceEntry($entry->fresh('seller'));
    }

    public function deletePrice(User $actor, int $priceId): array
    {
        $this->assertAdmin($actor);

        $entry = PriceEntry::query()->find($priceId);
        if (! $entry) {
            throw new BusinessException('가격 정보를 찾을 수 없습니다.', 'PRICE_ENTRY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $entry->delete();

        return ['message' => '가격 정보가 삭제되었습니다.'];
    }

    public function getPriceHistory(int $productId): array
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $entries = PriceEntry::query()
            ->where('product_id', $productId)
            ->orderByDesc('collected_at')
            ->orderByDesc('id')
            ->get();

        $prices = $entries->pluck('price')->map(fn ($value) => (float) $value);

        return [
            'productId' => $product->id,
            'productName' => $product->name,
            'allTimeLowest' => $prices->min(),
            'allTimeHighest' => $prices->max(),
            'history' => $entries->map(fn (PriceEntry $entry): array => [
                'date' => optional($entry->collected_at)->format('Y-m-d'),
                'lowestPrice' => (float) $entry->price,
                'averagePrice' => (float) $entry->price,
            ])->values()->all(),
        ];
    }

    public function listAlerts(User $user): array
    {
        return PriceAlert::query()
            ->with('product')
            ->where('user_id', $user->id)
            ->orderByDesc('id')
            ->get()
            ->map(fn (PriceAlert $alert): array => $this->serializeAlert($alert))
            ->values()
            ->all();
    }

    public function createAlert(User $user, array $payload): array
    {
        if (PriceAlert::query()->where('user_id', $user->id)->where('product_id', $payload['productId'])->exists()) {
            throw new BusinessException('해당 상품에 대한 알림이 이미 존재합니다.', 'ALERT_EXISTS', Response::HTTP_CONFLICT);
        }

        $currentLowestPrice = PriceEntry::query()
            ->where('product_id', $payload['productId'])
            ->min('price');

        $alert = PriceAlert::query()->create([
            'user_id' => $user->id,
            'product_id' => $payload['productId'],
            'target_price' => $payload['targetPrice'],
            'current_lowest_price' => $currentLowestPrice,
            'is_triggered' => $currentLowestPrice !== null && $currentLowestPrice <= $payload['targetPrice'],
        ]);

        return $this->serializeAlert($alert->fresh('product'));
    }

    public function deleteAlert(User $user, int $alertId): array
    {
        $alert = PriceAlert::query()->where('user_id', $user->id)->find($alertId);
        if (! $alert) {
            throw new BusinessException('가격 알림을 찾을 수 없습니다.', 'PRICE_ALERT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $alert->delete();

        return ['message' => '가격 알림이 삭제되었습니다.'];
    }

    private function serializePriceEntry(PriceEntry $entry): array
    {
        return [
            'id' => $entry->id,
            'seller' => $entry->seller ? [
                'id' => $entry->seller->id,
                'name' => $entry->seller->name,
                'rating' => (float) $entry->seller->rating,
            ] : null,
            'price' => (float) $entry->price,
            'shippingCost' => (float) $entry->shipping_fee,
            'stockStatus' => $entry->stock_status,
            'isCardDiscount' => (bool) $entry->is_card_discount,
            'isCashDiscount' => (bool) $entry->is_cash_discount,
            'updatedAt' => optional($entry->updated_at)?->toISOString(),
        ];
    }

    private function serializeAlert(PriceAlert $alert): array
    {
        return [
            'id' => $alert->id,
            'productId' => $alert->product_id,
            'productName' => $alert->product?->name,
            'targetPrice' => (float) $alert->target_price,
            'currentLowestPrice' => $alert->current_lowest_price !== null ? (float) $alert->current_lowest_price : null,
            'isTriggered' => (bool) $alert->is_triggered,
            'createdAt' => optional($alert->created_at)?->toISOString(),
        ];
    }

    private function assertSellerOrAdmin(User $user): void
    {
        if (! in_array($user->role, ['SELLER', 'ADMIN'], true)) {
            throw new BusinessException('판매자 또는 관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
