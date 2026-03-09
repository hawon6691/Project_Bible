<?php

namespace App\Modules\Cart\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\CartItem;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class CartService
{
    public function list(User $user): array
    {
        return CartItem::query()
            ->with(['product.category', 'seller'])
            ->where('user_id', $user->id)
            ->orderByDesc('id')
            ->get()
            ->map(fn (CartItem $item): array => $this->serialize($item))
            ->values()
            ->all();
    }

    public function add(User $user, array $payload): array
    {
        $existing = CartItem::query()
            ->where('user_id', $user->id)
            ->where('product_id', $payload['productId'])
            ->where('seller_id', $payload['sellerId'])
            ->where('selected_options', $payload['selectedOptions'] ?? null)
            ->first();

        if ($existing) {
            $existing->increment('quantity', (int) $payload['quantity']);

            return $this->serialize($existing->fresh(['product.category', 'seller']));
        }

        $item = CartItem::query()->create([
            'user_id' => $user->id,
            'product_id' => $payload['productId'],
            'seller_id' => $payload['sellerId'],
            'quantity' => (int) $payload['quantity'],
            'selected_options' => $payload['selectedOptions'] ?? null,
        ]);

        return $this->serialize($item->fresh(['product.category', 'seller']));
    }

    public function update(User $user, int $itemId, int $quantity): array
    {
        $item = CartItem::query()->where('user_id', $user->id)->find($itemId);
        if (! $item) {
            throw new BusinessException('장바구니 항목을 찾을 수 없습니다.', 'CART_ITEM_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $item->forceFill(['quantity' => $quantity])->save();

        return $this->serialize($item->fresh(['product.category', 'seller']));
    }

    public function delete(User $user, int $itemId): array
    {
        $item = CartItem::query()->where('user_id', $user->id)->find($itemId);
        if (! $item) {
            throw new BusinessException('장바구니 항목을 찾을 수 없습니다.', 'CART_ITEM_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $item->delete();

        return ['message' => '장바구니 항목이 삭제되었습니다.'];
    }

    public function clear(User $user): array
    {
        CartItem::query()->where('user_id', $user->id)->delete();

        return ['message' => '장바구니가 비워졌습니다.'];
    }

    private function serialize(CartItem $item): array
    {
        $priceEntry = $item->product?->priceEntries()
            ->where('seller_id', $item->seller_id)
            ->orderBy('price')
            ->first();

        return [
            'id' => $item->id,
            'productId' => $item->product_id,
            'productName' => $item->product?->name,
            'thumbnailUrl' => $item->product?->thumbnail_url,
            'sellerId' => $item->seller_id,
            'sellerName' => $item->seller?->name,
            'quantity' => $item->quantity,
            'selectedOptions' => $item->selected_options,
            'unitPrice' => $priceEntry?->price !== null ? (float) $priceEntry->price : null,
            'createdAt' => optional($item->created_at)?->toISOString(),
            'updatedAt' => optional($item->updated_at)?->toISOString(),
        ];
    }
}
