<?php

namespace App\Modules\Wishlist\Services;

use App\Models\User;
use App\Models\WishlistItem;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;

class WishlistService
{
    public function list(User $user, array $filters): array
    {
        $page = max((int) ($filters['page'] ?? 1), 1);
        $limit = min(max((int) ($filters['limit'] ?? 20), 1), 100);

        /** @var LengthAwarePaginator $paginator */
        $paginator = WishlistItem::query()
            ->with('product')
            ->where('user_id', $user->id)
            ->orderByDesc('id')
            ->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => array_map(fn (WishlistItem $item): array => $this->serialize($item), $paginator->items()),
            'pagination' => [
                'page' => $paginator->currentPage(),
                'limit' => $paginator->perPage(),
                'totalCount' => $paginator->total(),
                'totalPages' => $paginator->lastPage(),
            ],
        ];
    }

    public function toggle(User $user, int $productId): array
    {
        $existing = WishlistItem::query()->where('user_id', $user->id)->where('product_id', $productId)->first();

        if ($existing) {
            $existing->delete();

            return ['wishlisted' => false];
        }

        WishlistItem::query()->create([
            'user_id' => $user->id,
            'product_id' => $productId,
        ]);

        return ['wishlisted' => true];
    }

    public function delete(User $user, int $productId): array
    {
        WishlistItem::query()->where('user_id', $user->id)->where('product_id', $productId)->delete();

        return ['message' => '위시리스트에서 제거되었습니다.'];
    }

    private function serialize(WishlistItem $item): array
    {
        return [
            'id' => $item->id,
            'productId' => $item->product_id,
            'productName' => $item->product?->name,
            'thumbnailUrl' => $item->product?->thumbnail_url,
            'createdAt' => optional($item->created_at)?->toISOString(),
        ];
    }
}
