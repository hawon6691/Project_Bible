<?php

namespace App\Modules\Spec\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Product;
use App\Models\ProductSpec;
use App\Models\SpecDefinition;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class SpecService
{
    public function listDefinitions(array $filters): array
    {
        return SpecDefinition::query()
            ->when(! empty($filters['categoryId']), fn ($query) => $query->where('category_id', $filters['categoryId']))
            ->orderBy('sort_order')
            ->orderBy('id')
            ->get()
            ->map(fn (SpecDefinition $definition): array => $this->serializeDefinition($definition))
            ->values()
            ->all();
    }

    public function createDefinition(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);

        $definition = SpecDefinition::query()->create([
            'category_id' => $payload['categoryId'],
            'name' => $payload['name'],
            'type' => $payload['type'] ?? 'TEXT',
            'options' => $payload['options'] ?? null,
            'unit' => $payload['unit'] ?? null,
            'sort_order' => (int) ($payload['sortOrder'] ?? 0),
        ]);

        return $this->serializeDefinition($definition);
    }

    public function updateDefinition(User $actor, int $id, array $payload): array
    {
        $this->assertAdmin($actor);

        $definition = SpecDefinition::query()->find($id);
        if (! $definition) {
            throw new BusinessException('스펙 정의를 찾을 수 없습니다.', 'SPEC_DEFINITION_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $updates = [];
        if (array_key_exists('name', $payload)) {
            $updates['name'] = $payload['name'];
        }
        if (array_key_exists('type', $payload)) {
            $updates['type'] = $payload['type'];
        }
        if (array_key_exists('options', $payload)) {
            $updates['options'] = $payload['options'];
        }
        if (array_key_exists('unit', $payload)) {
            $updates['unit'] = $payload['unit'];
        }
        if (array_key_exists('sortOrder', $payload)) {
            $updates['sort_order'] = (int) $payload['sortOrder'];
        }

        if ($updates !== []) {
            $definition->forceFill($updates)->save();
        }

        return $this->serializeDefinition($definition->fresh());
    }

    public function deleteDefinition(User $actor, int $id): array
    {
        $this->assertAdmin($actor);

        $definition = SpecDefinition::query()->find($id);
        if (! $definition) {
            throw new BusinessException('스펙 정의를 찾을 수 없습니다.', 'SPEC_DEFINITION_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $definition->delete();

        return ['message' => '스펙 정의가 삭제되었습니다.'];
    }

    public function getProductSpecs(int $productId): array
    {
        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return ProductSpec::query()
            ->where('product_id', $productId)
            ->orderBy('sort_order')
            ->orderBy('id')
            ->get()
            ->map(fn (ProductSpec $spec): array => $this->serializeProductSpec($spec))
            ->values()
            ->all();
    }

    public function setProductSpecs(User $actor, int $productId, array $payload): array
    {
        $this->assertAdmin($actor);

        $product = Product::query()->find($productId);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        ProductSpec::query()->where('product_id', $productId)->delete();

        foreach ($payload['specs'] as $spec) {
            ProductSpec::query()->create([
                'product_id' => $productId,
                'spec_key' => $spec['name'],
                'spec_value' => $spec['value'],
                'sort_order' => (int) ($spec['sortOrder'] ?? 0),
            ]);
        }

        return $this->getProductSpecs($productId);
    }

    private function serializeDefinition(SpecDefinition $definition): array
    {
        return [
            'id' => $definition->id,
            'categoryId' => $definition->category_id,
            'name' => $definition->name,
            'type' => $definition->type,
            'options' => $definition->options,
            'unit' => $definition->unit,
            'sortOrder' => $definition->sort_order,
            'createdAt' => optional($definition->created_at)?->toISOString(),
            'updatedAt' => optional($definition->updated_at)?->toISOString(),
        ];
    }

    private function serializeProductSpec(ProductSpec $spec): array
    {
        return [
            'id' => $spec->id,
            'name' => $spec->spec_key,
            'value' => $spec->spec_value,
            'sortOrder' => $spec->sort_order,
        ];
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
