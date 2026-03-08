<?php

namespace App\Modules\Seller\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Seller;
use App\Models\User;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Symfony\Component\HttpFoundation\Response;

class SellerService
{
    public function list(array $filters): array
    {
        $page = max((int) ($filters['page'] ?? 1), 1);
        $limit = min(max((int) ($filters['limit'] ?? 20), 1), 100);

        /** @var LengthAwarePaginator $paginator */
        $paginator = Seller::query()
            ->orderByDesc('id')
            ->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => array_map(fn (Seller $seller): array => $this->serializeSeller($seller), $paginator->items()),
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
        $seller = Seller::query()->find($id);
        if (! $seller) {
            throw new BusinessException('판매처를 찾을 수 없습니다.', 'SELLER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $this->serializeSeller($seller);
    }

    public function create(User $actor, array $payload): array
    {
        $this->assertAdmin($actor);

        $seller = Seller::query()->create([
            'name' => $payload['name'],
            'code' => $payload['code'],
            'status' => $payload['status'] ?? 'ACTIVE',
            'rating' => $payload['rating'] ?? 0,
            'contact_email' => $payload['contactEmail'] ?? null,
            'homepage_url' => $payload['homepageUrl'] ?? null,
        ]);

        return $this->serializeSeller($seller);
    }

    public function update(User $actor, int $id, array $payload): array
    {
        $this->assertAdmin($actor);

        $seller = Seller::query()->find($id);
        if (! $seller) {
            throw new BusinessException('판매처를 찾을 수 없습니다.', 'SELLER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $updates = [];
        if (array_key_exists('name', $payload)) {
            $updates['name'] = $payload['name'];
        }
        if (array_key_exists('code', $payload)) {
            $updates['code'] = $payload['code'];
        }
        if (array_key_exists('status', $payload)) {
            $updates['status'] = $payload['status'];
        }
        if (array_key_exists('rating', $payload)) {
            $updates['rating'] = $payload['rating'];
        }
        if (array_key_exists('contactEmail', $payload)) {
            $updates['contact_email'] = $payload['contactEmail'];
        }
        if (array_key_exists('homepageUrl', $payload)) {
            $updates['homepage_url'] = $payload['homepageUrl'];
        }

        if ($updates !== []) {
            $seller->forceFill($updates)->save();
        }

        return $this->serializeSeller($seller->fresh());
    }

    public function delete(User $actor, int $id): array
    {
        $this->assertAdmin($actor);

        $seller = Seller::query()->find($id);
        if (! $seller) {
            throw new BusinessException('판매처를 찾을 수 없습니다.', 'SELLER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $seller->delete();

        return ['message' => '판매처가 삭제되었습니다.'];
    }

    private function serializeSeller(Seller $seller): array
    {
        return [
            'id' => $seller->id,
            'name' => $seller->name,
            'code' => $seller->code,
            'status' => $seller->status,
            'rating' => (float) $seller->rating,
            'contactEmail' => $seller->contact_email,
            'homepageUrl' => $seller->homepage_url,
            'createdAt' => optional($seller->created_at)?->toISOString(),
            'updatedAt' => optional($seller->updated_at)?->toISOString(),
        ];
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
