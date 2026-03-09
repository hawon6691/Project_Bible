<?php

namespace App\Modules\Auto\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\AutoLeaseOffer;
use App\Models\AutoModel;
use App\Models\AutoOption;
use App\Models\AutoTrim;
use Symfony\Component\HttpFoundation\Response;

class AutoService
{
    public function models(?string $brand, ?string $type): array
    {
        return AutoModel::query()
            ->when($brand, fn ($q) => $q->where('brand', $brand))
            ->when($type, fn ($q) => $q->where('type', $type))
            ->orderBy('id')
            ->get()
            ->map(fn (AutoModel $model): array => [
                'id' => $model->id,
                'brand' => $model->brand,
                'name' => $model->name,
                'type' => $model->type,
            ])->all();
    }

    public function trims(int $modelId): array
    {
        $this->findModel($modelId);

        return AutoTrim::query()->where('auto_model_id', $modelId)->orderBy('id')->get()->map(fn (AutoTrim $trim): array => [
            'id' => $trim->id,
            'name' => $trim->name,
            'basePrice' => (float) $trim->base_price,
        ])->all();
    }

    public function estimate(array $payload): array
    {
        $model = $this->findModel((int) $payload['modelId']);
        $trim = AutoTrim::query()->where('auto_model_id', $model->id)->find($payload['trimId']);
        if (! $trim) {
            throw new BusinessException('트림을 찾을 수 없습니다.', 'AUTO_TRIM_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $optionPrice = AutoOption::query()->where('auto_trim_id', $trim->id)->whereIn('id', $payload['optionIds'] ?? [])->sum('price');
        $base = (float) $trim->base_price;
        $tax = round(($base + $optionPrice) * 0.07, 2);
        $total = round($base + $optionPrice + $tax, 2);

        return [
            'basePrice' => $base,
            'optionPrice' => (float) $optionPrice,
            'tax' => $tax,
            'totalPrice' => $total,
            'monthlyPayment' => round($total / 36, 2),
        ];
    }

    public function leaseOffers(int $modelId): array
    {
        $this->findModel($modelId);

        return AutoLeaseOffer::query()->where('auto_model_id', $modelId)->orderBy('monthly_payment')->get()->map(fn (AutoLeaseOffer $offer): array => [
            'id' => $offer->id,
            'provider' => $offer->provider,
            'monthlyPayment' => (float) $offer->monthly_payment,
            'contractMonths' => $offer->contract_months,
        ])->all();
    }

    private function findModel(int $modelId): AutoModel
    {
        $model = AutoModel::query()->find($modelId);
        if (! $model) {
            throw new BusinessException('자동차 모델을 찾을 수 없습니다.', 'AUTO_MODEL_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $model;
    }
}
