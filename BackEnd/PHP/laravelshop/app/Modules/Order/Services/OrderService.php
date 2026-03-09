<?php

namespace App\Modules\Order\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Address;
use App\Models\CartItem;
use App\Models\Order;
use App\Models\OrderItem;
use App\Models\PriceEntry;
use App\Models\User;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class OrderService
{
    public function create(User $user, array $payload): array
    {
        $address = Address::query()->where('user_id', $user->id)->find($payload['addressId']);
        if (! $address) {
            throw new BusinessException('배송지를 찾을 수 없습니다.', 'ADDRESS_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return DB::transaction(function () use ($user, $payload, $address): array {
            $items = $this->resolveItems($user, $payload);

            if ($items === []) {
                throw new BusinessException('주문 항목이 비어 있습니다.', 'ORDER_ITEMS_REQUIRED', Response::HTTP_BAD_REQUEST);
            }

            $totalAmount = 0.0;

            $order = Order::query()->create([
                'user_id' => $user->id,
                'address_id' => $address->id,
                'order_number' => $this->generateOrderNumber(),
                'status' => 'ORDER_PLACED',
                'point_used' => (float) ($payload['usePoint'] ?? 0),
                'recipient_name' => $address->recipient_name,
                'phone' => $address->phone,
                'zip_code' => $address->zip_code,
                'address_line1' => $address->address_line1,
                'address_line2' => $address->address_line2,
                'memo' => $payload['memo'] ?? $address->memo,
            ]);

            foreach ($items as $item) {
                $lineTotal = ($item['unitPrice'] + $item['shippingFee']) * $item['quantity'];
                $totalAmount += $lineTotal;

                OrderItem::query()->create([
                    'order_id' => $order->id,
                    'product_id' => $item['productId'],
                    'seller_id' => $item['sellerId'],
                    'product_name' => $item['productName'],
                    'quantity' => $item['quantity'],
                    'unit_price' => $item['unitPrice'],
                    'shipping_fee' => $item['shippingFee'],
                    'selected_options' => $item['selectedOptions'],
                ]);
            }

            $finalAmount = max($totalAmount - (float) ($payload['usePoint'] ?? 0), 0);

            $order->forceFill([
                'total_amount' => $totalAmount,
                'final_amount' => $finalAmount,
            ])->save();

            if (($payload['fromCart'] ?? false) === true && ! empty($payload['cartItemIds'])) {
                CartItem::query()
                    ->where('user_id', $user->id)
                    ->whereIn('id', $payload['cartItemIds'])
                    ->delete();
            }

            return $this->detailForUser($user, $order->id);
        });
    }

    public function listForUser(User $user, array $filters): array
    {
        return $this->paginateOrders(
            Order::query()->where('user_id', $user->id)->orderByDesc('id'),
            $filters
        );
    }

    public function detailForUser(User $user, int $orderId): array
    {
        $order = Order::query()
            ->with(['items.product', 'items.seller'])
            ->where('user_id', $user->id)
            ->find($orderId);

        if (! $order) {
            throw new BusinessException('주문을 찾을 수 없습니다.', 'ORDER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $this->serializeDetail($order);
    }

    public function cancel(User $user, int $orderId): array
    {
        $order = Order::query()->where('user_id', $user->id)->find($orderId);
        if (! $order) {
            throw new BusinessException('주문을 찾을 수 없습니다.', 'ORDER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $order->forceFill([
            'status' => 'CANCELLED',
            'cancelled_at' => now(),
        ])->save();

        return $this->detailForUser($user, $orderId);
    }

    public function listForAdmin(User $actor, array $filters): array
    {
        $this->assertAdmin($actor);

        return $this->paginateOrders(
            Order::query()->with('user')->orderByDesc('id'),
            $filters
        );
    }

    public function updateStatus(User $actor, int $orderId, string $status): array
    {
        $this->assertAdmin($actor);

        $order = Order::query()->find($orderId);
        if (! $order) {
            throw new BusinessException('주문을 찾을 수 없습니다.', 'ORDER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $order->forceFill(['status' => $status])->save();

        return $this->serializeDetail($order->fresh(['items.product', 'items.seller']));
    }

    private function resolveItems(User $user, array $payload): array
    {
        if (($payload['fromCart'] ?? false) === true) {
            $cartItems = CartItem::query()
                ->with(['product', 'seller'])
                ->where('user_id', $user->id)
                ->whereIn('id', $payload['cartItemIds'] ?? [])
                ->get();

            return $cartItems->map(function (CartItem $item): array {
                $priceEntry = PriceEntry::query()
                    ->where('product_id', $item->product_id)
                    ->where('seller_id', $item->seller_id)
                    ->orderBy('price')
                    ->first();

                return [
                    'productId' => $item->product_id,
                    'sellerId' => $item->seller_id,
                    'productName' => $item->product?->name ?? 'Unknown Product',
                    'quantity' => $item->quantity,
                    'selectedOptions' => $item->selected_options,
                    'unitPrice' => (float) ($priceEntry?->price ?? 0),
                    'shippingFee' => (float) ($priceEntry?->shipping_fee ?? 0),
                ];
            })->values()->all();
        }

        return collect($payload['items'] ?? [])->map(function (array $item): array {
            $priceEntry = PriceEntry::query()
                ->where('product_id', $item['productId'])
                ->where('seller_id', $item['sellerId'])
                ->orderBy('price')
                ->first();

            return [
                'productId' => $item['productId'],
                'sellerId' => $item['sellerId'],
                'productName' => $priceEntry?->product?->name ?? 'Unknown Product',
                'quantity' => (int) $item['quantity'],
                'selectedOptions' => $item['selectedOptions'] ?? null,
                'unitPrice' => (float) ($priceEntry?->price ?? 0),
                'shippingFee' => (float) ($priceEntry?->shipping_fee ?? 0),
            ];
        })->values()->all();
    }

    private function paginateOrders($query, array $filters): array
    {
        $page = max((int) ($filters['page'] ?? 1), 1);
        $limit = min(max((int) ($filters['limit'] ?? 20), 1), 100);

        /** @var LengthAwarePaginator $paginator */
        $paginator = $query->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => array_map(fn (Order $order): array => $this->serializeSummary($order), $paginator->items()),
            'pagination' => [
                'page' => $paginator->currentPage(),
                'limit' => $paginator->perPage(),
                'totalCount' => $paginator->total(),
                'totalPages' => $paginator->lastPage(),
            ],
        ];
    }

    private function serializeSummary(Order $order): array
    {
        return [
            'id' => $order->id,
            'orderNumber' => $order->order_number,
            'status' => $order->status,
            'totalAmount' => (float) $order->total_amount,
            'pointUsed' => (float) $order->point_used,
            'finalAmount' => (float) $order->final_amount,
            'createdAt' => optional($order->created_at)?->toISOString(),
        ];
    }

    private function serializeDetail(Order $order): array
    {
        return [
            'id' => $order->id,
            'orderNumber' => $order->order_number,
            'status' => $order->status,
            'items' => $order->items->map(fn (OrderItem $item): array => [
                'id' => $item->id,
                'productId' => $item->product_id,
                'productName' => $item->product_name,
                'sellerId' => $item->seller_id,
                'sellerName' => $item->seller?->name,
                'quantity' => $item->quantity,
                'unitPrice' => (float) $item->unit_price,
                'shippingFee' => (float) $item->shipping_fee,
                'selectedOptions' => $item->selected_options,
            ])->values()->all(),
            'totalAmount' => (float) $order->total_amount,
            'pointUsed' => (float) $order->point_used,
            'finalAmount' => (float) $order->final_amount,
            'shippingAddress' => [
                'recipientName' => $order->recipient_name,
                'phone' => $order->phone,
                'zipCode' => $order->zip_code,
                'addressLine1' => $order->address_line1,
                'addressLine2' => $order->address_line2,
                'memo' => $order->memo,
            ],
            'createdAt' => optional($order->created_at)?->toISOString(),
            'updatedAt' => optional($order->updated_at)?->toISOString(),
        ];
    }

    private function generateOrderNumber(): string
    {
        return 'ORD-'.now()->format('Ymd').'-'.Str::upper(Str::random(6));
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
