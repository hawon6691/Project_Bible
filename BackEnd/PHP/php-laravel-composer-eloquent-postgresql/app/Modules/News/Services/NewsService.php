<?php

namespace App\Modules\News\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\News;
use App\Models\NewsCategory;
use App\Models\NewsProduct;
use App\Models\Product;
use App\Models\User;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class NewsService
{
    public function list(?int $categoryId, int $page, int $limit): array
    {
        $result = News::query()->when($categoryId !== null, fn ($q) => $q->where('category_id', $categoryId))->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (News $news): array => $this->serializeNews($news))->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    public function categories(): array
    {
        return NewsCategory::query()->orderBy('id')->get()->map(fn (NewsCategory $category): array => [
            'id' => $category->id,
            'name' => $category->name,
            'slug' => $category->slug,
        ])->all();
    }

    public function show(int $id): array
    {
        $news = News::query()->find($id);
        if (! $news) {
            throw new BusinessException('뉴스를 찾을 수 없습니다.', 'NEWS_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $productIds = NewsProduct::query()->where('news_id', $news->id)->pluck('product_id');
        $products = Product::query()->whereIn('id', $productIds)->get()->map(fn (Product $product): array => [
            'id' => $product->id,
            'name' => $product->name,
            'slug' => $product->slug,
        ])->all();

        return array_merge($this->serializeNews($news), ['products' => $products]);
    }

    public function create(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);
        $news = News::query()->create([
            'category_id' => $payload['categoryId'] ?? null,
            'author_id' => $actor->id,
            'title' => $payload['title'],
            'content' => $payload['content'],
            'thumbnail_url' => $payload['thumbnailUrl'] ?? null,
        ]);
        $this->syncProducts($news->id, $payload['productIds'] ?? []);

        return $this->show($news->id);
    }

    public function update(User $actor, int $id, array $payload): array
    {
        $this->assertAdmin($actor);
        $news = News::query()->find($id);
        if (! $news) {
            throw new BusinessException('뉴스를 찾을 수 없습니다.', 'NEWS_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $news->forceFill([
            'category_id' => array_key_exists('categoryId', $payload) ? $payload['categoryId'] : $news->category_id,
            'title' => $payload['title'] ?? $news->title,
            'content' => $payload['content'] ?? $news->content,
            'thumbnail_url' => array_key_exists('thumbnailUrl', $payload) ? $payload['thumbnailUrl'] : $news->thumbnail_url,
        ])->save();

        if (array_key_exists('productIds', $payload)) {
            $this->syncProducts($news->id, $payload['productIds'] ?? []);
        }

        return $this->show($news->id);
    }

    public function delete(User $actor, int $id): array
    {
        $this->assertAdmin($actor);
        $news = News::query()->find($id);
        if (! $news) {
            throw new BusinessException('뉴스를 찾을 수 없습니다.', 'NEWS_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $news->delete();

        return ['message' => '뉴스가 삭제되었습니다.'];
    }

    public function createCategory(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);
        $category = NewsCategory::query()->create([
            'name' => $payload['name'],
            'slug' => Str::slug($payload['slug'] ?: $payload['name']),
        ]);

        return [
            'id' => $category->id,
            'name' => $category->name,
            'slug' => $category->slug,
        ];
    }

    public function deleteCategory(User $actor, int $id): array
    {
        $this->assertAdmin($actor);
        $category = NewsCategory::query()->find($id);
        if (! $category) {
            throw new BusinessException('뉴스 카테고리를 찾을 수 없습니다.', 'NEWS_CATEGORY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $category->delete();

        return ['message' => '뉴스 카테고리가 삭제되었습니다.'];
    }

    private function syncProducts(int $newsId, array $productIds): void
    {
        NewsProduct::query()->where('news_id', $newsId)->delete();
        foreach ($productIds as $productId) {
            if (Product::query()->where('id', $productId)->exists()) {
                NewsProduct::query()->create(['news_id' => $newsId, 'product_id' => $productId]);
            }
        }
    }

    private function serializeNews(News $news): array
    {
        return [
            'id' => $news->id,
            'categoryId' => $news->category_id,
            'authorId' => $news->author_id,
            'title' => $news->title,
            'content' => $news->content,
            'thumbnailUrl' => $news->thumbnail_url,
            'createdAt' => optional($news->created_at)?->toISOString(),
            'updatedAt' => optional($news->updated_at)?->toISOString(),
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
