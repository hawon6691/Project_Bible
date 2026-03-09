<?php

namespace App\Modules\Prediction\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Product;
use Symfony\Component\HttpFoundation\Response;

class PredictionService
{
    public function predictProductPrice(int $productId, int $days = 30): array
    {
        $product = Product::query()->with('priceEntries')->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $days = max(1, min($days, 90));
        $prices = $product->priceEntries->sortBy('collected_at')->values();
        $currentPrice = (float) ($prices->min('price') ?? 0);
        $lastPrices = $prices->take(-min(7, max(1, $prices->count())));
        $avgRecent = (float) ($lastPrices->avg('price') ?? $currentPrice);
        $trendDelta = $currentPrice - $avgRecent;
        $trend = abs($trendDelta) < 1 ? 'STABLE' : ($trendDelta < 0 ? 'FALLING' : 'RISING');
        $trendStrength = $avgRecent > 0 ? round(min(1, abs($trendDelta) / $avgRecent), 2) : 0;

        $predictions = [];
        $base = $currentPrice;
        for ($i = 1; $i <= min($days, 7); $i++) {
            $projected = match ($trend) {
                'FALLING' => max(0, $base - (abs($trendDelta) * 0.6 * $i)),
                'RISING' => $base + (abs($trendDelta) * 0.6 * $i),
                default => $base,
            };

            $predictions[] = [
                'date' => now()->addDays($i)->toDateString(),
                'predictedPrice' => round($projected, 2),
                'confidence' => round(max(0.5, 0.9 - ($i * 0.04)), 2),
            ];
        }

        return [
            'productId' => $product->id,
            'productName' => $product->name,
            'currentPrice' => $currentPrice,
            'predictions' => $predictions,
            'trend' => $trend,
            'trendStrength' => $trendStrength,
            'movingAverage7d' => round((float) ($lastPrices->avg('price') ?? $currentPrice), 2),
            'movingAverage30d' => round((float) ($prices->avg('price') ?? $currentPrice), 2),
            'seasonalityNote' => $trend === 'FALLING' ? '단기 하락 패턴이 감지되었습니다.' : '가격 변동성이 크지 않습니다.',
            'recommendation' => $trend === 'FALLING' ? 'BUY_SOON' : ($trend === 'RISING' ? 'BUY_NOW' : 'HOLD'),
            'recommendationReason' => $trend === 'FALLING'
                ? '추가 하락 가능성이 있어 단기 추이를 지켜본 뒤 구매를 권장합니다.'
                : ($trend === 'RISING' ? '상승 추세가 감지되어 빠른 구매가 유리합니다.' : '현재 가격대가 안정적입니다.'),
            'updatedAt' => now()->toISOString(),
        ];
    }
}
