<?php

namespace App\Modules\PcBuilder\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\PcBuild;
use App\Models\PcBuildPart;
use App\Models\PcCompatibilityRule;
use App\Models\Product;
use App\Models\User;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class PcBuilderService
{
    public function listMine(User $user, int $page, int $limit): array
    {
        $result = PcBuild::query()->where('user_id', $user->id)->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (PcBuild $build): array => $this->serializeBuildSummary($build))->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    public function create(User $user, array $payload): array
    {
        $build = PcBuild::query()->create([
            'user_id' => $user->id,
            'name' => $payload['name'],
            'description' => $payload['description'] ?? null,
        ]);

        return $this->serializeBuildDetail($build->fresh());
    }

    public function show(int $id): array
    {
        $build = PcBuild::query()->with('parts.product')->find($id);
        if (! $build) {
            throw new BusinessException('견적을 찾을 수 없습니다.', 'PC_BUILD_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $build->increment('view_count');

        return $this->serializeBuildDetail($build->fresh('parts.product'));
    }

    public function update(User $user, int $id, array $payload): array
    {
        $build = $this->findOwnedBuild($user, $id);
        $build->forceFill([
            'name' => $payload['name'] ?? $build->name,
            'description' => $payload['description'] ?? $build->description,
        ])->save();

        return $this->serializeBuildDetail($build->fresh('parts.product'));
    }

    public function delete(User $user, int $id): array
    {
        $build = $this->findOwnedBuild($user, $id);
        $build->delete();

        return ['message' => '견적이 삭제되었습니다.'];
    }

    public function addPart(User $user, int $buildId, array $payload): array
    {
        $build = $this->findOwnedBuild($user, $buildId);
        $product = Product::query()->find($payload['productId']);
        if (! $product) {
            throw new BusinessException('상품을 찾을 수 없습니다.', 'PRODUCT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        PcBuildPart::query()->create([
            'pc_build_id' => $build->id,
            'part_type' => $payload['partType'],
            'product_id' => $product->id,
            'quantity' => (int) ($payload['quantity'] ?? 1),
        ]);

        return $this->serializeBuildDetail($build->fresh('parts.product'));
    }

    public function removePart(User $user, int $buildId, int $partId): array
    {
        $build = $this->findOwnedBuild($user, $buildId);
        $part = PcBuildPart::query()->where('pc_build_id', $build->id)->find($partId);
        if (! $part) {
            throw new BusinessException('부품을 찾을 수 없습니다.', 'PC_BUILD_PART_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $part->delete();

        return $this->serializeBuildDetail($build->fresh('parts.product'));
    }

    public function compatibility(int $buildId): array
    {
        $build = PcBuild::query()->with('parts.product')->find($buildId);
        if (! $build) {
            throw new BusinessException('견적을 찾을 수 없습니다.', 'PC_BUILD_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $issues = [];
        $parts = $build->parts;
        foreach (PcCompatibilityRule::query()->get() as $rule) {
            $source = $parts->firstWhere('part_type', $rule->source_part_type);
            $target = $parts->firstWhere('part_type', $rule->target_part_type);
            if (! $source || ! $target) {
                continue;
            }
            if ($rule->rule_type === 'CATEGORY_MISMATCH' && $source->product?->category_id === $target->product?->category_id) {
                $issues[] = [
                    'ruleId' => $rule->id,
                    'message' => $rule->name,
                    'severity' => 'WARNING',
                ];
            }
        }

        return ['compatible' => $issues === [], 'issues' => $issues];
    }

    public function share(User $user, int $buildId): array
    {
        $build = $this->findOwnedBuild($user, $buildId);
        if (! $build->share_code) {
            $build->forceFill(['share_code' => Str::lower(Str::random(12))])->save();
        }

        return [
            'shareUrl' => '/api/v1/pc-builds/shared/'.$build->share_code,
            'shareCode' => $build->share_code,
        ];
    }

    public function showShared(string $shareCode): array
    {
        $build = PcBuild::query()->with('parts.product')->where('share_code', $shareCode)->first();
        if (! $build) {
            throw new BusinessException('공유 견적을 찾을 수 없습니다.', 'PC_BUILD_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $this->serializeBuildDetail($build);
    }

    public function popular(int $page, int $limit): array
    {
        $result = PcBuild::query()->orderByDesc('view_count')->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (PcBuild $build): array => $this->serializeBuildSummary($build))->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    public function listRules(User $user): array
    {
        $this->assertAdmin($user);

        return PcCompatibilityRule::query()->orderBy('id')->get()->map(fn (PcCompatibilityRule $rule): array => $this->serializeRule($rule))->all();
    }

    public function createRule(User $user, array $payload): array
    {
        $this->assertAdmin($user);

        $rule = PcCompatibilityRule::query()->create([
            'name' => $payload['name'],
            'source_part_type' => $payload['sourcePartType'],
            'target_part_type' => $payload['targetPartType'],
            'rule_type' => $payload['ruleType'],
            'rule_value' => $payload['ruleValue'] ?? null,
        ]);

        return $this->serializeRule($rule);
    }

    public function updateRule(User $user, int $id, array $payload): array
    {
        $this->assertAdmin($user);
        $rule = PcCompatibilityRule::query()->find($id);
        if (! $rule) {
            throw new BusinessException('호환성 규칙을 찾을 수 없습니다.', 'COMPATIBILITY_RULE_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $rule->forceFill([
            'name' => $payload['name'] ?? $rule->name,
            'source_part_type' => $payload['sourcePartType'] ?? $rule->source_part_type,
            'target_part_type' => $payload['targetPartType'] ?? $rule->target_part_type,
            'rule_type' => $payload['ruleType'] ?? $rule->rule_type,
            'rule_value' => array_key_exists('ruleValue', $payload) ? $payload['ruleValue'] : $rule->rule_value,
        ])->save();

        return $this->serializeRule($rule);
    }

    public function deleteRule(User $user, int $id): array
    {
        $this->assertAdmin($user);
        $rule = PcCompatibilityRule::query()->find($id);
        if (! $rule) {
            throw new BusinessException('호환성 규칙을 찾을 수 없습니다.', 'COMPATIBILITY_RULE_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $rule->delete();

        return ['message' => '호환성 규칙이 삭제되었습니다.'];
    }

    private function findOwnedBuild(User $user, int $id): PcBuild
    {
        $build = PcBuild::query()->with('parts.product')->where('user_id', $user->id)->find($id);
        if (! $build) {
            throw new BusinessException('견적을 찾을 수 없습니다.', 'PC_BUILD_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $build;
    }

    private function serializeBuildSummary(PcBuild $build): array
    {
        return [
            'id' => $build->id,
            'name' => $build->name,
            'description' => $build->description,
            'shareCode' => $build->share_code,
            'viewCount' => $build->view_count,
            'createdAt' => optional($build->created_at)?->toISOString(),
            'updatedAt' => optional($build->updated_at)?->toISOString(),
        ];
    }

    private function serializeBuildDetail(PcBuild $build): array
    {
        return array_merge($this->serializeBuildSummary($build), [
            'userId' => $build->user_id,
            'parts' => $build->parts->map(fn (PcBuildPart $part): array => [
                'id' => $part->id,
                'partType' => $part->part_type,
                'quantity' => $part->quantity,
                'product' => $part->product ? [
                    'id' => $part->product->id,
                    'name' => $part->product->name,
                    'slug' => $part->product->slug,
                ] : null,
            ])->all(),
        ]);
    }

    private function serializeRule(PcCompatibilityRule $rule): array
    {
        return [
            'id' => $rule->id,
            'name' => $rule->name,
            'sourcePartType' => $rule->source_part_type,
            'targetPartType' => $rule->target_part_type,
            'ruleType' => $rule->rule_type,
            'ruleValue' => $rule->rule_value,
            'createdAt' => optional($rule->created_at)?->toISOString(),
            'updatedAt' => optional($rule->updated_at)?->toISOString(),
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
