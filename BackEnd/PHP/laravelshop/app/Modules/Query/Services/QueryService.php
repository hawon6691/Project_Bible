<?php

namespace App\Modules\Query\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Product;
use App\Models\ProductQueryView;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class QueryService
{
    public function listProducts(int $page, int $limit): array
    {
        $result = Product::query()->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);
        return ['items' => $result->getCollection()->map(fn (Product $product): array => $this->serialize($product))->all(), 'pagination' => ['page' => $result->currentPage(), 'limit' => $result->perPage(), 'total' => $result->total(), 'totalPages' => $result->lastPage()]];
    }

    public function showProduct(int $productId): array
    {
        $product = Product::query()->with('specs', 'priceEntries')->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        return $this->serialize($product, true);
    }

    public function sync(User $user, int $productId): array
    {
        $this->assertAdmin($user);
        $view = ProductQueryView::query()->updateOrCreate(['product_id' => $productId], ['view_count' => 1, 'search_keywords' => ['manual-sync']]);
        return ['message' => 'Query view가 동기화되었습니다.', 'productId' => $view->product_id];
    }

    public function rebuild(User $user): array
    {
        $this->assertAdmin($user);
        foreach (Product::query()->get() as $product) {
            ProductQueryView::query()->updateOrCreate(['product_id' => $product->id], ['view_count' => 1, 'search_keywords' => ['rebuild']]);
        }
        return ['message' => 'Query view 재구성이 완료되었습니다.', 'count' => Product::query()->count()];
    }

    private function serialize(Product $product, bool $detail = false): array
    {
        $payload = ['id' => $product->id, 'name' => $product->name, 'slug' => $product->slug, 'status' => $product->status];
        if ($detail) {
            $payload['specs'] = $product->specs->map(fn ($spec) => ['name' => $spec->name, 'value' => $spec->value])->all();
            $payload['priceEntries'] = $product->priceEntries->map(fn ($entry) => ['price' => $entry->price, 'sellerId' => $entry->seller_id])->all();
        }
        return $payload;
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
