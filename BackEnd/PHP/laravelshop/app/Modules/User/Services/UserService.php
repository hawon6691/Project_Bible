<?php

namespace App\Modules\User\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\User;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;
use Symfony\Component\HttpFoundation\Response;

class UserService
{
    public function getMe(User $user): array
    {
        return $this->serializeUser($user);
    }

    public function updateMe(User $user, array $payload): array
    {
        $updates = [];

        if (array_key_exists('name', $payload)) {
            $updates['name'] = $payload['name'];
        }

        if (array_key_exists('phone', $payload)) {
            $updates['phone'] = $payload['phone'];
        }

        if (! empty($payload['password'])) {
            $updates['password'] = Hash::make($payload['password']);
        }

        if ($updates !== []) {
            $user->forceFill($updates)->save();
        }

        return $this->serializeUser($user->fresh());
    }

    public function deleteMe(User $user): array
    {
        $user->delete();

        return ['message' => '회원 탈퇴가 완료되었습니다.'];
    }

    public function getProfile(int $id): array
    {
        $user = User::query()->find($id);

        if (! $user) {
            throw new BusinessException('사용자를 찾을 수 없습니다.', 'USER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $this->serializeProfile($user);
    }

    public function updateProfile(User $user, array $payload): array
    {
        $updates = [];

        if (array_key_exists('nickname', $payload)) {
            $updates['nickname'] = $payload['nickname'];
        }

        if (array_key_exists('bio', $payload)) {
            $updates['bio'] = $payload['bio'];
        }

        if ($updates !== []) {
            $user->forceFill($updates)->save();
        }

        return $this->serializeProfile($user->fresh());
    }

    public function uploadProfileImage(User $user, UploadedFile $image): array
    {
        if ($user->profile_image_url) {
            $this->deleteStoredProfileImage($user->profile_image_url);
        }

        $path = $image->store('profile-images', 'public');
        $imageUrl = Storage::disk('public')->url($path);

        $user->forceFill([
            'profile_image_url' => $imageUrl,
        ])->save();

        return ['imageUrl' => $imageUrl];
    }

    public function deleteProfileImage(User $user): array
    {
        if ($user->profile_image_url) {
            $this->deleteStoredProfileImage($user->profile_image_url);
        }

        $user->forceFill([
            'profile_image_url' => null,
        ])->save();

        return ['message' => '프로필 이미지가 삭제되었습니다.'];
    }

    public function listUsers(User $actor, array $filters): array
    {
        $this->assertAdmin($actor);

        $page = max((int) ($filters['page'] ?? 1), 1);
        $limit = min(max((int) ($filters['limit'] ?? 20), 1), 100);

        $query = User::query()->orderByDesc('id');

        if (! empty($filters['search'])) {
            $search = trim((string) $filters['search']);
            $query->where(function ($builder) use ($search): void {
                $builder
                    ->where('email', 'like', "%{$search}%")
                    ->orWhere('name', 'like', "%{$search}%");
            });
        }

        if (! empty($filters['status'])) {
            $query->where('status', $filters['status']);
        }

        if (! empty($filters['role'])) {
            $query->where('role', $filters['role']);
        }

        /** @var LengthAwarePaginator $paginator */
        $paginator = $query->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => array_map(
                fn (User $user): array => $this->serializeUser($user),
                $paginator->items()
            ),
            'pagination' => [
                'page' => $paginator->currentPage(),
                'limit' => $paginator->perPage(),
                'total' => $paginator->total(),
                'totalPages' => $paginator->lastPage(),
            ],
        ];
    }

    public function updateUserStatus(User $actor, int $userId, string $status): array
    {
        $this->assertAdmin($actor);

        $user = User::query()->find($userId);

        if (! $user) {
            throw new BusinessException('사용자를 찾을 수 없습니다.', 'USER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $user->forceFill([
            'status' => $status,
        ])->save();

        return $this->serializeUser($user->fresh());
    }

    private function serializeUser(User $user): array
    {
        return [
            'id' => $user->id,
            'email' => $user->email,
            'name' => $user->name,
            'nickname' => $user->nickname,
            'role' => $user->role,
            'status' => $user->status,
            'phone' => $user->phone,
            'profileImageUrl' => $user->profile_image_url,
            'bio' => $user->bio,
            'emailVerifiedAt' => optional($user->email_verified_at)?->toISOString(),
            'lastLoginAt' => optional($user->last_login_at)?->toISOString(),
            'createdAt' => optional($user->created_at)?->toISOString(),
            'updatedAt' => optional($user->updated_at)?->toISOString(),
        ];
    }

    private function serializeProfile(User $user): array
    {
        return [
            'id' => $user->id,
            'nickname' => $user->nickname,
            'name' => $user->name,
            'bio' => $user->bio,
            'profileImageUrl' => $user->profile_image_url,
        ];
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }

    private function deleteStoredProfileImage(string $imageUrl): void
    {
        $prefix = Storage::disk('public')->url('');
        $relativePath = str_starts_with($imageUrl, $prefix)
            ? substr($imageUrl, strlen($prefix))
            : ltrim($imageUrl, '/');

        if ($relativePath !== '') {
            Storage::disk('public')->delete($relativePath);
        }
    }
}
