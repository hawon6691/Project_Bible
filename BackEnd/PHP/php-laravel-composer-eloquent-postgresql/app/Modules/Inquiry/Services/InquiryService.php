<?php

namespace App\Modules\Inquiry\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Product;
use App\Models\ProductInquiry;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class InquiryService
{
    public function listByProduct(int $productId, ?User $actor = null): array
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return ProductInquiry::query()
            ->with('user')
            ->where('product_id', $productId)
            ->orderByDesc('id')
            ->get()
            ->map(fn (ProductInquiry $inquiry): array => $this->serialize($inquiry, $actor))
            ->values()
            ->all();
    }

    public function create(User $user, int $productId, array $payload): array
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $inquiry = ProductInquiry::query()->create([
            'product_id' => $productId,
            'user_id' => $user->id,
            'title' => $payload['title'],
            'content' => $payload['content'],
            'is_secret' => (bool) ($payload['isSecret'] ?? false),
        ]);

        return $this->serialize($inquiry->fresh('user'), $user);
    }

    public function answer(User $actor, int $inquiryId, array $payload): array
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('문의 답변 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $inquiry = ProductInquiry::query()->with('user')->find($inquiryId);
        if (! $inquiry) {
            throw new BusinessException('문의글을 찾을 수 없습니다.', 'INQUIRY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $inquiry->forceFill([
            'answer' => $payload['answer'],
            'answered_by' => $actor->id,
            'answered_at' => now(),
        ])->save();

        return $this->serialize($inquiry->fresh('user'), $actor);
    }

    public function myInquiries(User $user): array
    {
        return ProductInquiry::query()
            ->with('user')
            ->where('user_id', $user->id)
            ->orderByDesc('id')
            ->get()
            ->map(fn (ProductInquiry $inquiry): array => $this->serialize($inquiry, $user))
            ->values()
            ->all();
    }

    public function delete(User $actor, int $inquiryId): array
    {
        $inquiry = ProductInquiry::query()->find($inquiryId);
        if (! $inquiry) {
            throw new BusinessException('문의글을 찾을 수 없습니다.', 'INQUIRY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        if ($actor->role !== 'ADMIN' && $actor->id !== $inquiry->user_id) {
            throw new BusinessException('문의글 삭제 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $inquiry->delete();

        return ['message' => '문의글이 삭제되었습니다.'];
    }

    private function serialize(ProductInquiry $inquiry, ?User $actor): array
    {
        $canViewContent = ! $inquiry->is_secret || ($actor && ($actor->role === 'ADMIN' || $actor->id === $inquiry->user_id));

        return [
            'id' => $inquiry->id,
            'productId' => $inquiry->product_id,
            'title' => $inquiry->title,
            'content' => $canViewContent ? $inquiry->content : '비밀 문의입니다.',
            'isSecret' => $inquiry->is_secret,
            'answer' => $inquiry->answer,
            'answeredAt' => optional($inquiry->answered_at)?->toISOString(),
            'author' => $inquiry->user ? [
                'id' => $inquiry->user->id,
                'name' => $inquiry->user->name,
                'nickname' => $inquiry->user->nickname,
            ] : null,
            'createdAt' => optional($inquiry->created_at)?->toISOString(),
            'updatedAt' => optional($inquiry->updated_at)?->toISOString(),
        ];
    }
}
