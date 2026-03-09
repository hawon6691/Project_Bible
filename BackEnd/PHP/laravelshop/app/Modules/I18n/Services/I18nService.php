<?php

namespace App\Modules\I18n\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\ExchangeRate;
use App\Models\Translation;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class I18nService
{
    public function translations(?string $locale = null, ?string $namespace = null): array
    {
        $query = Translation::query()->orderBy('id');
        if ($locale) {
            $query->where('locale', $locale);
        }
        if ($namespace) {
            $query->where('namespace', $namespace);
        }

        return $query->get()->map(fn (Translation $translation): array => [
            'id' => $translation->id,
            'locale' => $translation->locale,
            'namespace' => $translation->namespace,
            'key' => $translation->key,
            'value' => $translation->value,
        ])->values()->all();
    }

    public function upsertTranslation(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);
        $translation = Translation::query()->updateOrCreate(
            [
                'locale' => $payload['locale'],
                'namespace' => $payload['namespace'],
                'key' => $payload['key'],
            ],
            ['value' => $payload['value']],
        );

        return [
            'id' => $translation->id,
            'locale' => $translation->locale,
            'namespace' => $translation->namespace,
            'key' => $translation->key,
            'value' => $translation->value,
        ];
    }

    public function deleteTranslation(User $actor, int $id): array
    {
        $this->assertAdmin($actor);
        $translation = Translation::query()->find($id);
        if (! $translation) {
            throw new BusinessException('번역을 찾을 수 없습니다.', 'TRANSLATION_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        $translation->delete();
        return ['message' => '번역이 삭제되었습니다.'];
    }

    public function exchangeRates(): array
    {
        return ExchangeRate::query()->orderBy('id')->get()->map(fn (ExchangeRate $rate): array => [
            'id' => $rate->id,
            'baseCurrency' => $rate->base_currency,
            'targetCurrency' => $rate->target_currency,
            'rate' => $rate->rate,
            'updatedAt' => optional($rate->updated_at_exchange ?? $rate->updated_at)?->toISOString(),
        ])->values()->all();
    }

    public function upsertExchangeRate(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);
        $rate = ExchangeRate::query()->updateOrCreate(
            [
                'base_currency' => strtoupper($payload['baseCurrency']),
                'target_currency' => strtoupper($payload['targetCurrency']),
            ],
            [
                'rate' => $payload['rate'],
                'updated_at_exchange' => now(),
            ],
        );

        return [
            'id' => $rate->id,
            'baseCurrency' => $rate->base_currency,
            'targetCurrency' => $rate->target_currency,
            'rate' => $rate->rate,
            'updatedAt' => optional($rate->updated_at_exchange)?->toISOString(),
        ];
    }

    public function convert(float $amount, string $from, string $to): array
    {
        $rate = ExchangeRate::query()
            ->where('base_currency', strtoupper($from))
            ->where('target_currency', strtoupper($to))
            ->first();
        if (! $rate) {
            throw new BusinessException('환율 정보를 찾을 수 없습니다.', 'EXCHANGE_RATE_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return [
            'originalAmount' => $amount,
            'originalCurrency' => strtoupper($from),
            'convertedAmount' => round($amount * $rate->rate, 2),
            'targetCurrency' => strtoupper($to),
            'rate' => $rate->rate,
        ];
    }

    private function assertAdmin(User $actor): void
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
