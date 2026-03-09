<?php

namespace App\Modules\Matching\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Product;
use App\Models\ProductMapping;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class MatchingService
{
    public function pending(User $actor, int $page, int $limit): array
    {
        $this->assertAdmin($actor);
        $result = ProductMapping::query()->where('status', 'PENDING')->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (ProductMapping $mapping): array => $this->serialize($mapping))->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    public function approve(User $actor, int $id, int $productId): array
    {
        $this->assertAdmin($actor);
        $mapping = ProductMapping::query()->find($id);
        $product = Product::query()->find($productId);
        if (! $mapping) {
            throw new BusinessException('매핑 대상을 찾을 수 없습니다.', 'MAPPING_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $mapping->forceFill(['product_id' => $productId, 'status' => 'APPROVED', 'reason' => null])->save();

        return ['message' => '매핑을 승인했습니다.'];
    }

    public function reject(User $actor, int $id, string $reason): array
    {
        $this->assertAdmin($actor);
        $mapping = ProductMapping::query()->find($id);
        if (! $mapping) {
            throw new BusinessException('매핑 대상을 찾을 수 없습니다.', 'MAPPING_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $mapping->forceFill(['status' => 'REJECTED', 'reason' => $reason])->save();

        return ['message' => '매핑을 거절했습니다.'];
    }

    public function autoMatch(User $actor): array
    {
        $this->assertAdmin($actor);
        $pending = ProductMapping::query()->where('status', 'PENDING')->get();
        $matched = 0;

        foreach ($pending as $mapping) {
            $product = Product::query()->where('name', 'like', '%' . $mapping->source_name . '%')->first();
            if ($product) {
                $mapping->forceFill(['product_id' => $product->id, 'status' => 'APPROVED', 'reason' => 'AUTO_MATCHED'])->save();
                $matched++;
            }
        }

        return [
            'matchedCount' => $matched,
            'pendingCount' => ProductMapping::query()->where('status', 'PENDING')->count(),
        ];
    }

    public function stats(User $actor): array
    {
        $this->assertAdmin($actor);

        return [
            'pending' => ProductMapping::query()->where('status', 'PENDING')->count(),
            'approved' => ProductMapping::query()->where('status', 'APPROVED')->count(),
            'rejected' => ProductMapping::query()->where('status', 'REJECTED')->count(),
        ];
    }

    private function serialize(ProductMapping $mapping): array
    {
        return [
            'id' => $mapping->id,
            'sourceName' => $mapping->source_name,
            'productId' => $mapping->product_id,
            'status' => $mapping->status,
            'reason' => $mapping->reason,
            'createdAt' => optional($mapping->created_at)?->toISOString(),
            'updatedAt' => optional($mapping->updated_at)?->toISOString(),
        ];
    }

    private function pagination($result): array
    {
        return [
            'page' => $result->currentPage(),
            'limit' => $result->perPage(),
            'total' => $result->total(),
            'totalPages' => $result->lastPage(),
        ];
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
