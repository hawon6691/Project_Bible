<?php

namespace App\Modules\Category\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Category;
use App\Models\User;
use Illuminate\Support\Collection;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class CategoryService
{
    public function listTree(): array
    {
        $categories = Category::query()
            ->orderBy('depth')
            ->orderBy('sort_order')
            ->orderBy('id')
            ->get();

        return $this->buildTree($categories);
    }

    public function getById(int $id): array
    {
        $category = Category::query()->find($id);

        if (! $category) {
            throw new BusinessException('카테고리를 찾을 수 없습니다.', 'CATEGORY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $this->serializeCategory($category, true);
    }

    public function create(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);

        $parent = null;
        $parentId = $payload['parentId'] ?? null;

        if ($parentId !== null) {
            $parent = Category::query()->find($parentId);

            if (! $parent) {
                throw new BusinessException('부모 카테고리를 찾을 수 없습니다.', 'CATEGORY_NOT_FOUND', Response::HTTP_NOT_FOUND);
            }
        }

        $category = Category::query()->create([
            'parent_id' => $parent?->id,
            'name' => $payload['name'],
            'slug' => $this->makeUniqueSlug($payload['name']),
            'depth' => $parent ? $parent->depth + 1 : 0,
            'sort_order' => (int) ($payload['sortOrder'] ?? 0),
            'is_visible' => (bool) ($payload['isVisible'] ?? true),
        ]);

        return $this->serializeCategory($category->fresh(), true);
    }

    public function update(User $actor, int $id, array $payload): array
    {
        $this->assertAdmin($actor);

        $category = Category::query()->find($id);

        if (! $category) {
            throw new BusinessException('카테고리를 찾을 수 없습니다.', 'CATEGORY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $updates = [];

        if (array_key_exists('name', $payload)) {
            $updates['name'] = $payload['name'];
            $updates['slug'] = $this->makeUniqueSlug($payload['name'], $category->id);
        }

        if (array_key_exists('sortOrder', $payload)) {
            $updates['sort_order'] = (int) $payload['sortOrder'];
        }

        if (array_key_exists('isVisible', $payload)) {
            $updates['is_visible'] = (bool) $payload['isVisible'];
        }

        if ($updates !== []) {
            $category->forceFill($updates)->save();
        }

        return $this->serializeCategory($category->fresh(), true);
    }

    public function delete(User $actor, int $id): array
    {
        $this->assertAdmin($actor);

        $category = Category::query()->find($id);

        if (! $category) {
            throw new BusinessException('카테고리를 찾을 수 없습니다.', 'CATEGORY_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        if (Category::query()->where('parent_id', $category->id)->exists()) {
            throw new BusinessException('하위 카테고리가 존재하여 삭제할 수 없습니다.', 'CATEGORY_HAS_CHILDREN', Response::HTTP_BAD_REQUEST);
        }

        $category->delete();

        return ['message' => '카테고리가 삭제되었습니다.'];
    }

    /**
     * @param  Collection<int, Category>  $categories
     * @return array<int, array<string, mixed>>
     */
    private function buildTree(Collection $categories): array
    {
        $byParent = $categories->groupBy('parent_id');

        $walker = function (?int $parentId) use (&$walker, $byParent): array {
            return $byParent
                ->get($parentId, collect())
                ->map(fn (Category $category): array => array_merge(
                    $this->serializeCategory($category),
                    ['children' => $walker($category->id)]
                ))
                ->values()
                ->all();
        };

        return $walker(null);
    }

    private function serializeCategory(Category $category, bool $includeParent = false): array
    {
        $payload = [
            'id' => $category->id,
            'parentId' => $category->parent_id,
            'name' => $category->name,
            'slug' => $category->slug,
            'depth' => $category->depth,
            'sortOrder' => $category->sort_order,
            'isVisible' => $category->is_visible,
            'createdAt' => optional($category->created_at)?->toISOString(),
            'updatedAt' => optional($category->updated_at)?->toISOString(),
        ];

        if ($includeParent) {
            $payload['parent'] = $category->parent
                ? [
                    'id' => $category->parent->id,
                    'name' => $category->parent->name,
                    'slug' => $category->parent->slug,
                ]
                : null;
        }

        return $payload;
    }

    private function makeUniqueSlug(string $name, ?int $ignoreId = null): string
    {
        $base = Str::slug($name, '-');
        $base = $base !== '' ? $base : 'category';
        $slug = $base;
        $suffix = 1;

        while (
            Category::query()
                ->when($ignoreId !== null, fn ($query) => $query->where('id', '!=', $ignoreId))
                ->where('slug', $slug)
                ->exists()
        ) {
            $slug = $base.'-'.$suffix;
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
