<?php

namespace App\Modules\Review\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Order;
use App\Models\Product;
use App\Models\Review;
use App\Models\User;
use App\Modules\Point\Services\PointService;
use Symfony\Component\HttpFoundation\Response;

class ReviewService
{
    public function __construct(
        private readonly PointService $pointService,
    ) {
    }

    public function listByProduct(int $productId): array
    {
        return Review::query()
            ->with('user')
            ->where('product_id', $productId)
            ->orderByDesc('id')
            ->get()
            ->map(fn (Review $review): array => $this->serialize($review))
            ->values()
            ->all();
    }

    public function create(User $user, int $productId, array $payload): array
    {
        $order = Order::query()->with('items')->where('user_id', $user->id)->find($payload['orderId']);
        if (! $order) {
            throw new BusinessException('주문을 찾을 수 없습니다.', 'ORDER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $hasProduct = $order->items->contains(fn ($item) => $item->product_id === $productId);
        if (! $hasProduct) {
            throw new BusinessException('주문에 포함되지 않은 상품입니다.', 'ORDER_PRODUCT_MISMATCH', Response::HTTP_BAD_REQUEST);
        }

        $review = Review::query()->create([
            'user_id' => $user->id,
            'product_id' => $productId,
            'order_id' => $order->id,
            'rating' => $payload['rating'],
            'content' => $payload['content'],
        ]);

        $this->recalculateProductReviewStats($productId);
        $this->pointService->rewardReview($user, $review->id);

        return $this->serialize($review->fresh('user'));
    }

    public function update(User $user, int $reviewId, array $payload): array
    {
        $review = Review::query()->with('user')->find($reviewId);
        if (! $review) {
            throw new BusinessException('리뷰를 찾을 수 없습니다.', 'REVIEW_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        if ($review->user_id !== $user->id) {
            throw new BusinessException('리뷰 수정 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $updates = [];
        if (array_key_exists('rating', $payload)) {
            $updates['rating'] = $payload['rating'];
        }
        if (array_key_exists('content', $payload)) {
            $updates['content'] = $payload['content'];
        }

        if ($updates !== []) {
            $review->forceFill($updates)->save();
            $this->recalculateProductReviewStats($review->product_id);
        }

        return $this->serialize($review->fresh('user'));
    }

    public function delete(User $actor, int $reviewId): array
    {
        $review = Review::query()->find($reviewId);
        if (! $review) {
            throw new BusinessException('리뷰를 찾을 수 없습니다.', 'REVIEW_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        if ($actor->role !== 'ADMIN' && $review->user_id !== $actor->id) {
            throw new BusinessException('리뷰 삭제 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $productId = $review->product_id;
        $review->delete();
        $this->recalculateProductReviewStats($productId);

        return ['message' => '리뷰가 삭제되었습니다.'];
    }

    private function recalculateProductReviewStats(int $productId): void
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            return;
        }

        $query = Review::query()->where('product_id', $productId);
        $product->forceFill([
            'review_count' => $query->count(),
            'rating_avg' => round((float) ($query->avg('rating') ?? 0), 2),
        ])->save();
    }

    private function serialize(Review $review): array
    {
        return [
            'id' => $review->id,
            'productId' => $review->product_id,
            'orderId' => $review->order_id,
            'rating' => $review->rating,
            'content' => $review->content,
            'author' => $review->user ? [
                'id' => $review->user->id,
                'name' => $review->user->name,
                'nickname' => $review->user->nickname,
            ] : null,
            'createdAt' => optional($review->created_at)?->toISOString(),
            'updatedAt' => optional($review->updated_at)?->toISOString(),
        ];
    }
}
