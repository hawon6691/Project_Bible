<?php

namespace App\Modules\Product\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Category;
use App\Models\PriceEntry;
use App\Models\Product;
use App\Models\ProductSpec;
use App\Models\User;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class ProductService
{
    public function list(array $filters): array
    {
        $page = max((int) ($filters['page'] ?? 1), 1);
        $limit = min(max((int) ($filters['limit'] ?? 20), 1), 100);
        $sort = $filters['sort'] ?? 'newest';

        $query = Product::query()
            ->with('category')
            ->withMin('priceEntries as lowest_price', 'price')
            ->withCount('priceEntries as seller_count')
            ->when(! empty($filters['categoryId']), fn (Builder $q) => $q->where('category_id', $filters['categoryId']))
            ->when(! empty($filters['search']), fn (Builder $q) => $q->where('name', 'like', '%' . trim((string) $filters['search']) . '%'))
            ->when(isset($filters['minPrice']), function (Builder $q) use ($filters): void {
                $q->whereHas('priceEntries', fn (Builder $sub) => $sub->where('price', '>=', $filters['minPrice']));
            })
            ->when(isset($filters['maxPrice']), function (Builder $q) use ($filters): void {
                $q->whereHas('priceEntries', fn (Builder $sub) => $sub->where('price', '<=', $filters['maxPrice']));
            });

        match ($sort) {
            'price_asc' => $query->orderBy('lowest_price')->orderBy('id'),
            'price_desc' => $query->orderByDesc('lowest_price')->orderByDesc('id'),
            'rating', 'rating_desc' => $query->orderByDesc('rating_avg')->orderByDesc('review_count')->orderByDesc('id'),
            'rating_asc' => $query->orderBy('rating_avg')->orderBy('review_count')->orderBy('id'),
            'popularity' => $query->orderByDesc(DB::raw('(review_count * 10) + rating_avg'))->orderByDesc('id'),
            default => $query->orderByDesc('id'),
        };

        /** @var LengthAwarePaginator $paginator */
        $paginator = $query->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => array_map(
                fn (Product $product): array => $this->serializeSummary($product),
                $paginator->items()
            ),
            'pagination' => [
                'page' => $paginator->currentPage(),
                'limit' => $paginator->perPage(),
                'totalCount' => $paginator->total(),
                'totalPages' => $paginator->lastPage(),
            ],
        ];
    }

    public function detail(int $id): array
    {
        $product = Product::query()
            ->with(['category', 'specs', 'priceEntries.seller'])
            ->find($id);

        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $this->serializeDetail($product);
    }

    public function store(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);

        $category = Category::query()->find($payload['categoryId']);
        if (! $category) {
            throw new BusinessException('카테고리를 찾을 수 없습니다.', 'CATEGORY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $product = Product::query()->create([
            'category_id' => $category->id,
            'name' => $payload['name'],
            'slug' => $this->makeUniqueSlug($payload['name']),
            'description' => $payload['description'] ?? null,
            'brand' => $payload['brand'] ?? null,
            'status' => $payload['status'] ?? 'ACTIVE',
            'thumbnail_url' => $payload['thumbnailUrl'] ?? null,
        ]);

        return $this->detail($product->id);
    }

    public function update(User $actor, int $id, array $payload): array
    {
        $this->assertAdmin($actor);

        $product = Product::query()->find($id);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $updates = [];

        if (array_key_exists('categoryId', $payload)) {
            $category = Category::query()->find($payload['categoryId']);
            if (! $category) {
                throw new BusinessException('카테고리를 찾을 수 없습니다.', 'CATEGORY_NOT_FOUND', Response::HTTP_NOT_FOUND);
            }
            $updates['category_id'] = $category->id;
        }

        if (array_key_exists('name', $payload)) {
            $updates['name'] = $payload['name'];
            $updates['slug'] = $this->makeUniqueSlug($payload['name'], $product->id);
        }

        if (array_key_exists('description', $payload)) {
            $updates['description'] = $payload['description'];
        }

        if (array_key_exists('brand', $payload)) {
            $updates['brand'] = $payload['brand'];
        }

        if (array_key_exists('status', $payload)) {
            $updates['status'] = $payload['status'];
        }

        if (array_key_exists('thumbnailUrl', $payload)) {
            $updates['thumbnail_url'] = $payload['thumbnailUrl'];
        }

        if ($updates !== []) {
            $product->forceFill($updates)->save();
        }

        return $this->detail($product->id);
    }

    public function delete(User $actor, int $id): array
    {
        $this->assertAdmin($actor);

        $product = Product::query()->find($id);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $product->delete();

        return ['message' => '상품이 삭제되었습니다.'];
    }

    private function serializeSummary(Product $product): array
    {
        $lowestPrice = $product->lowest_price !== null ? (float) $product->lowest_price : null;

        return [
            'id' => $product->id,
            'name' => $product->name,
            'brand' => $product->brand,
            'category' => $product->category ? [
                'id' => $product->category->id,
                'name' => $product->category->name,
            ] : null,
            'lowestPrice' => $lowestPrice,
            'sellerCount' => (int) ($product->seller_count ?? 0),
            'thumbnailUrl' => $product->thumbnail_url,
            'reviewCount' => (int) $product->review_count,
            'averageRating' => (float) $product->rating_avg,
            'createdAt' => optional($product->created_at)?->toISOString(),
        ];
    }

    private function serializeDetail(Product $product): array
    {
        $prices = $product->priceEntries->pluck('price')->map(fn ($value) => (float) $value);

        return [
            'id' => $product->id,
            'name' => $product->name,
            'slug' => $product->slug,
            'description' => $product->description,
            'brand' => $product->brand,
            'status' => $product->status,
            'thumbnailUrl' => $product->thumbnail_url,
            'lowestPrice' => $prices->min(),
            'highestPrice' => $prices->max(),
            'averagePrice' => $prices->isNotEmpty() ? round((float) $prices->avg(), 2) : null,
            'category' => $product->category ? [
                'id' => $product->category->id,
                'name' => $product->category->name,
                'slug' => $product->category->slug,
            ] : null,
            'specs' => $product->specs->map(fn (ProductSpec $spec): array => [
                'name' => $spec->spec_key,
                'value' => $spec->spec_value,
                'sortOrder' => $spec->sort_order,
            ])->values()->all(),
            'priceEntries' => $product->priceEntries->map(fn (PriceEntry $entry): array => [
                'id' => $entry->id,
                'seller' => $entry->seller ? [
                    'id' => $entry->seller->id,
                    'name' => $entry->seller->name,
                    'rating' => (float) $entry->seller->rating,
                ] : null,
                'price' => (float) $entry->price,
                'shippingFee' => (float) $entry->shipping_fee,
                'stockStatus' => $entry->stock_status,
                'isCardDiscount' => (bool) $entry->is_card_discount,
                'isCashDiscount' => (bool) $entry->is_cash_discount,
                'updatedAt' => optional($entry->updated_at)?->toISOString(),
            ])->values()->all(),
            'reviewCount' => (int) $product->review_count,
            'averageRating' => (float) $product->rating_avg,
            'createdAt' => optional($product->created_at)?->toISOString(),
            'updatedAt' => optional($product->updated_at)?->toISOString(),
        ];
    }

    private function makeUniqueSlug(string $name, ?int $ignoreId = null): string
    {
        $base = Str::slug($name, '-');
        $base = $base !== '' ? $base : 'product';
        $slug = $base;
        $suffix = 1;

        while (
            Product::query()
                ->when($ignoreId !== null, fn ($query) => $query->where('id', '!=', $ignoreId))
                ->where('slug', $slug)
                ->exists()
        ) {
            $slug = $base . '-' . $suffix;
            $suffix++;
        }

        return $slug;
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
