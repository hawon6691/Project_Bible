<?php

namespace App\Modules\Trust\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Seller;
use App\Models\TrustScoreHistory;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class TrustService
{
    public function getCurrentScore(int $sellerId): array
    {
        $seller = $this->findSeller($sellerId);
        $history = TrustScoreHistory::query()->where('seller_id', $sellerId)->orderByDesc('recorded_at')->first();

        if (! $history) {
            $history = $this->makeHistory($sellerId, (float) ($seller->rating ?? 4.0));
        }

        return $this->serialize($seller, $history);
    }

    public function getHistory(int $sellerId, int $limit = 20): array
    {
        $seller = $this->findSeller($sellerId);

        return TrustScoreHistory::query()->where('seller_id', $sellerId)->orderByDesc('recorded_at')->limit($limit)->get()
            ->map(fn (TrustScoreHistory $history): array => $this->serialize($seller, $history))
            ->values()->all();
    }

    public function recalculate(User $actor, int $sellerId): array
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $seller = $this->findSeller($sellerId);
        $history = $this->makeHistory($sellerId, (float) ($seller->rating ?? 4.0));

        return $this->serialize($seller, $history);
    }

    private function findSeller(int $sellerId): Seller
    {
        $seller = Seller::query()->find($sellerId);
        if (! $seller) {
            throw new BusinessException('판매처를 찾을 수 없습니다.', 'SELLER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $seller;
    }

    private function makeHistory(int $sellerId, float $rating): TrustScoreHistory
    {
        $score = max(0, min(100, (int) round($rating * 20)));
        $grade = match (true) {
            $score >= 95 => 'A+',
            $score >= 90 => 'A',
            $score >= 80 => 'B+',
            $score >= 70 => 'B',
            $score >= 60 => 'C',
            default => 'D',
        };

        return TrustScoreHistory::query()->create([
            'seller_id' => $sellerId,
            'score' => $score,
            'grade' => $grade,
            'trend' => 'STABLE',
            'breakdown' => [
                'deliveryScore' => $score,
                'priceAccuracy' => $score - 2,
                'responseTime' => 2.0,
                'reviewScore' => $rating,
            ],
            'recorded_at' => now(),
        ]);
    }

    private function serialize(Seller $seller, TrustScoreHistory $history): array
    {
        return [
            'sellerId' => $seller->id,
            'sellerName' => $seller->name,
            'overallScore' => $history->score,
            'grade' => $history->grade,
            'breakdown' => $history->breakdown,
            'trend' => $history->trend,
            'lastUpdatedAt' => optional($history->recorded_at)?->toISOString(),
        ];
    }
}
