<?php

namespace App\Modules\Recommendation\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Product;
use App\Models\Recommendation;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class RecommendationService
{
    public function trending(int $limit = 10): array
    {
        return Recommendation::query()
            ->with('product')
            ->where('is_active', true)
            ->where('type', 'TRENDING')
            ->orderByDesc('score')
            ->orderByDesc('id')
            ->limit(min(50, max(1, $limit)))
            ->get()
            ->map(fn (Recommendation $recommendation): array => $this->serialize($recommendation))
            ->values()
            ->all();
    }

    public function personal(User $user, int $limit = 10): array
    {
        $type = $user->role === 'ADMIN' ? 'TRENDING' : 'PERSONAL';
        $items = Recommendation::query()
            ->with('product')
            ->where('is_active', true)
            ->whereIn('type', [$type, 'TRENDING'])
            ->orderByDesc('score')
            ->orderByDesc('id')
            ->limit(min(50, max(1, $limit)))
            ->get();

        if ($items->isEmpty()) {
            $items = Product::query()->with('priceEntries')->orderByDesc('review_count')->limit(min(50, max(1, $limit)))->get()
                ->map(function (Product $product) {
                    $recommendation = new Recommendation([
                        'product_id' => $product->id,
                        'type' => 'PERSONAL',
                        'title' => $product->name,
                        'reason' => '리뷰와 반응이 좋은 상품입니다.',
                        'score' => (int) $product->review_count,
                        'is_active' => true,
                    ]);
                    $recommendation->setRelation('product', $product);

                    return $recommendation;
                });
        }

        return $items->map(fn ($item): array => $this->serialize($item))->values()->all();
    }

    public function adminList(User $actor): array
    {
        $this->assertAdmin($actor);

        return Recommendation::query()->with('product')->orderByDesc('id')->get()
            ->map(fn (Recommendation $recommendation): array => $this->serialize($recommendation))
            ->values()
            ->all();
    }

    public function create(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);

        $recommendation = Recommendation::query()->create([
            'product_id' => $payload['productId'],
            'type' => strtoupper($payload['type'] ?? 'TODAY'),
            'title' => $payload['title'] ?? null,
            'reason' => $payload['reason'] ?? null,
            'score' => $payload['score'] ?? 0,
            'is_active' => $payload['isActive'] ?? true,
        ]);

        return $this->serialize($recommendation->fresh('product'));
    }

    public function remove(User $actor, int $recommendationId): array
    {
        $this->assertAdmin($actor);

        $recommendation = Recommendation::query()->find($recommendationId);
        if (! $recommendation) {
            throw new BusinessException('추천 정보를 찾을 수 없습니다.', 'RECOMMENDATION_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $recommendation->delete();

        return ['message' => '추천이 삭제되었습니다.'];
    }

    private function assertAdmin(User $actor): void
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }

    private function serialize(Recommendation $recommendation): array
    {
        return [
            'id' => $recommendation->id,
            'type' => $recommendation->type,
            'title' => $recommendation->title,
            'reason' => $recommendation->reason,
            'score' => $recommendation->score,
            'isActive' => $recommendation->is_active,
            'product' => $recommendation->product ? [
                'id' => $recommendation->product->id,
                'name' => $recommendation->product->name,
                'thumbnailUrl' => $recommendation->product->thumbnail_url,
            ] : null,
            'createdAt' => optional($recommendation->created_at)?->toISOString(),
            'updatedAt' => optional($recommendation->updated_at)?->toISOString(),
        ];
    }
}
