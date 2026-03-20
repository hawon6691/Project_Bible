<?php

namespace App\Modules\Fraud\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\FraudFlag;
use App\Models\PriceEntry;
use App\Models\Product;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class FraudService
{
    public function getAlerts(?string $status = null): array
    {
        $query = FraudFlag::query()->orderByDesc('id');
        if ($status) {
            $query->where('status', strtoupper($status));
        }

        return $query->get()->map(fn (FraudFlag $flag): array => $this->serializeFlag($flag))->values()->all();
    }

    public function approveAlert(User $actor, int $flagId): array
    {
        $this->assertAdmin($actor);
        $flag = $this->findFlag($flagId);
        $flag->forceFill([
            'status' => 'APPROVED',
            'approved_by' => $actor->id,
            'approved_at' => now(),
        ])->save();

        return ['message' => '이상 가격 알림이 승인되었습니다.'];
    }

    public function rejectAlert(User $actor, int $flagId): array
    {
        $this->assertAdmin($actor);
        $flag = $this->findFlag($flagId);
        $flag->forceFill([
            'status' => 'REJECTED',
            'rejected_by' => $actor->id,
            'rejected_at' => now(),
        ])->save();

        return ['message' => '이상 가격 알림이 거절되었습니다.'];
    }

    public function getRealPrice(int $productId, ?int $sellerId = null): array
    {
        $entries = PriceEntry::query()->where('product_id', $productId);
        if ($sellerId) {
            $entries->where('seller_id', $sellerId);
        }

        $entry = $entries->orderByRaw('(price + shipping_fee) asc')->first();
        if (! $entry) {
            throw new BusinessException('가격 정보를 찾을 수 없습니다.', 'PRICE_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return [
            'productPrice' => $entry->price,
            'shippingFee' => $entry->shipping_fee,
            'totalPrice' => $entry->price + $entry->shipping_fee,
            'shippingType' => $entry->shipping_fee > 0 ? 'PAID' : 'FREE',
        ];
    }

    public function getEffectivePrices(int $productId): array
    {
        return PriceEntry::query()->where('product_id', $productId)->with('seller')->get()
            ->map(fn (PriceEntry $entry): array => [
                'sellerId' => $entry->seller_id,
                'sellerName' => $entry->seller?->name,
                'price' => $entry->price,
                'shippingFee' => $entry->shipping_fee,
                'totalPrice' => $entry->price + $entry->shipping_fee,
            ])->values()->all();
    }

    public function detectAnomalies(int $productId, bool $persist = false): array
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $entries = PriceEntry::query()->where('product_id', $productId)->get();
        $avg = (float) ($entries->avg('price') ?? 0);
        $items = [];

        foreach ($entries as $entry) {
            if ($avg > 0 && $entry->price < ($avg * 0.7)) {
                $items[] = [
                    'priceEntryId' => $entry->id,
                    'detectedPrice' => $entry->price,
                    'baselinePrice' => round($avg, 2),
                    'reason' => '평균 가격 대비 급격히 낮은 가격이 감지되었습니다.',
                ];

                if ($persist) {
                    FraudFlag::query()->create([
                        'product_id' => $productId,
                        'price_entry_id' => $entry->id,
                        'status' => 'PENDING',
                        'reason' => '평균 가격 대비 급격히 낮은 가격이 감지되었습니다.',
                        'detected_price' => $entry->price,
                        'baseline_price' => $avg,
                    ]);
                }
            }
        }

        return ['items' => $items];
    }

    public function getFlags(int $productId): array
    {
        return FraudFlag::query()->where('product_id', $productId)->orderByDesc('id')->get()
            ->map(fn (FraudFlag $flag): array => $this->serializeFlag($flag))->values()->all();
    }

    private function findFlag(int $flagId): FraudFlag
    {
        $flag = FraudFlag::query()->find($flagId);
        if (! $flag) {
            throw new BusinessException('이상 가격 알림을 찾을 수 없습니다.', 'FRAUD_ALERT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $flag;
    }

    private function assertAdmin(User $actor): void
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }

    private function serializeFlag(FraudFlag $flag): array
    {
        return [
            'id' => $flag->id,
            'productId' => $flag->product_id,
            'priceEntryId' => $flag->price_entry_id,
            'status' => $flag->status,
            'reason' => $flag->reason,
            'detectedPrice' => $flag->detected_price,
            'baselinePrice' => $flag->baseline_price,
            'createdAt' => optional($flag->created_at)?->toISOString(),
            'updatedAt' => optional($flag->updated_at)?->toISOString(),
        ];
    }
}
