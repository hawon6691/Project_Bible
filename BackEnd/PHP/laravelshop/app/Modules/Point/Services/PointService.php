<?php

namespace App\Modules\Point\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\PointTransaction;
use App\Models\User;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Symfony\Component\HttpFoundation\Response;

class PointService
{
    public function getBalance(User $user): array
    {
        $latest = PointTransaction::query()
            ->where('user_id', $user->id)
            ->latest('id')
            ->first();

        return [
            'balance' => $latest ? (float) $latest->balance : 0.0,
            'expiringSoon' => 0.0,
            'expiringDate' => null,
        ];
    }

    public function listTransactions(User $user, array $filters): array
    {
        $page = max((int) ($filters['page'] ?? 1), 1);
        $limit = min(max((int) ($filters['limit'] ?? 20), 1), 100);

        /** @var LengthAwarePaginator $paginator */
        $paginator = PointTransaction::query()
            ->where('user_id', $user->id)
            ->when(! empty($filters['type']), fn ($query) => $query->where('type', $filters['type']))
            ->orderByDesc('id')
            ->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => array_map(fn (PointTransaction $transaction): array => $this->serialize($transaction), $paginator->items()),
            'pagination' => [
                'page' => $paginator->currentPage(),
                'limit' => $paginator->perPage(),
                'totalCount' => $paginator->total(),
                'totalPages' => $paginator->lastPage(),
            ],
        ];
    }

    public function grantByAdmin(User $actor, array $payload): array
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $user = User::query()->find($payload['userId']);
        if (! $user) {
            throw new BusinessException('사용자를 찾을 수 없습니다.', 'USER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $this->appendTransaction(
            $user,
            'ADMIN_GRANT',
            (float) $payload['amount'],
            $payload['description']
        );
    }

    public function rewardReview(User $user, int $reviewId): array
    {
        return $this->appendTransaction(
            $user,
            'EARN',
            500,
            '리뷰 작성 적립 (#' . $reviewId . ')'
        );
    }

    private function appendTransaction(User $user, string $type, float $amount, string $description): array
    {
        $currentBalance = (float) (PointTransaction::query()->where('user_id', $user->id)->latest('id')->value('balance') ?? 0);
        $nextBalance = $currentBalance + $amount;

        $transaction = PointTransaction::query()->create([
            'user_id' => $user->id,
            'type' => $type,
            'amount' => $amount,
            'balance' => $nextBalance,
            'description' => $description,
        ]);

        return $this->serialize($transaction);
    }

    private function serialize(PointTransaction $transaction): array
    {
        return [
            'id' => $transaction->id,
            'type' => $transaction->type,
            'amount' => (float) $transaction->amount,
            'balance' => (float) $transaction->balance,
            'description' => $transaction->description,
            'createdAt' => optional($transaction->created_at)?->toISOString(),
        ];
    }
}
