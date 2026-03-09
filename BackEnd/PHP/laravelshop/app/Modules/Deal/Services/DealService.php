<?php

namespace App\Modules\Deal\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Deal;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class DealService
{
    public function list(?string $type = null): array
    {
        $query = Deal::query()->with('product')->orderByDesc('id');
        if ($type) {
            $query->where('type', strtoupper($type));
        }

        return $query->get()
            ->map(fn (Deal $deal): array => $this->serialize($deal))
            ->values()
            ->all();
    }

    public function create(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);

        $deal = Deal::query()->create([
            'product_id' => $payload['productId'],
            'title' => $payload['title'],
            'type' => strtoupper($payload['type'] ?? 'SPECIAL'),
            'description' => $payload['description'] ?? null,
            'deal_price' => $payload['dealPrice'],
            'discount_rate' => $payload['discountRate'] ?? 0,
            'stock' => $payload['stock'] ?? 0,
            'start_at' => $payload['startAt'],
            'end_at' => $payload['endAt'],
        ]);

        return $this->serialize($deal->fresh('product'));
    }

    public function update(User $actor, int $dealId, array $payload): array
    {
        $this->assertAdmin($actor);

        $deal = Deal::query()->with('product')->find($dealId);
        if (! $deal) {
            throw new BusinessException('특가 정보를 찾을 수 없습니다.', 'DEAL_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $updates = [];
        if (array_key_exists('productId', $payload)) {
            $updates['product_id'] = $payload['productId'];
        }
        if (array_key_exists('title', $payload)) {
            $updates['title'] = $payload['title'];
        }
        if (array_key_exists('type', $payload)) {
            $updates['type'] = strtoupper($payload['type']);
        }
        if (array_key_exists('description', $payload)) {
            $updates['description'] = $payload['description'];
        }
        if (array_key_exists('dealPrice', $payload)) {
            $updates['deal_price'] = $payload['dealPrice'];
        }
        if (array_key_exists('discountRate', $payload)) {
            $updates['discount_rate'] = $payload['discountRate'];
        }
        if (array_key_exists('stock', $payload)) {
            $updates['stock'] = $payload['stock'];
        }
        if (array_key_exists('startAt', $payload)) {
            $updates['start_at'] = $payload['startAt'];
        }
        if (array_key_exists('endAt', $payload)) {
            $updates['end_at'] = $payload['endAt'];
        }

        if ($updates !== []) {
            $deal->forceFill($updates)->save();
        }

        return $this->serialize($deal->fresh('product'));
    }

    public function remove(User $actor, int $dealId): array
    {
        $this->assertAdmin($actor);

        $deal = Deal::query()->find($dealId);
        if (! $deal) {
            throw new BusinessException('특가 정보를 찾을 수 없습니다.', 'DEAL_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $deal->delete();

        return ['message' => '특가가 삭제되었습니다.'];
    }

    private function assertAdmin(User $actor): void
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }

    private function serialize(Deal $deal): array
    {
        return [
            'id' => $deal->id,
            'product' => $deal->product ? [
                'id' => $deal->product->id,
                'name' => $deal->product->name,
                'thumbnailUrl' => $deal->product->thumbnail_url,
            ] : null,
            'title' => $deal->title,
            'type' => $deal->type,
            'description' => $deal->description,
            'dealPrice' => $deal->deal_price,
            'discountRate' => $deal->discount_rate,
            'stock' => $deal->stock,
            'startAt' => optional($deal->start_at)?->toISOString(),
            'endAt' => optional($deal->end_at)?->toISOString(),
            'createdAt' => optional($deal->created_at)?->toISOString(),
            'updatedAt' => optional($deal->updated_at)?->toISOString(),
        ];
    }
}
