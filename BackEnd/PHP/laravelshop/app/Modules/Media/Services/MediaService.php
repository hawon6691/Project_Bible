<?php

namespace App\Modules\Media\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\MediaAsset;
use App\Models\User;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class MediaService
{
    public function upload(User $user, array $payload): array
    {
        return collect($payload['files'])->map(function (array $file) use ($user, $payload): array {
            $asset = MediaAsset::query()->create([
                'user_id' => $user->id,
                'owner_type' => $payload['ownerType'] ?? 'SYSTEM',
                'owner_id' => $payload['ownerId'] ?? null,
                'file_name' => $file['fileName'],
                'file_path' => parse_url($file['fileUrl'], PHP_URL_PATH) ?: '/uploads/' . $file['fileName'],
                'file_url' => $file['fileUrl'],
                'mime_type' => $file['mimeType'],
                'size' => $file['size'],
            ]);

            return $this->serialize($asset);
        })->all();
    }

    public function presignedUrl(array $payload): array
    {
        $key = 'uploads/' . Str::random(16) . '-' . $payload['fileName'];

        return [
            'uploadUrl' => 'https://example.local/storage/' . $key,
            'fileKey' => $key,
        ];
    }

    public function stream(int $id): array
    {
        $asset = $this->find($id);

        return [
            'id' => $asset->id,
            'fileUrl' => $asset->file_url,
            'mimeType' => $asset->mime_type,
            'size' => $asset->size,
            'supportsRange' => str_starts_with($asset->mime_type, 'video/') || str_starts_with($asset->mime_type, 'audio/'),
        ];
    }

    public function metadata(int $id): array
    {
        $asset = $this->find($id);

        return [
            'mime' => $asset->mime_type,
            'size' => $asset->size,
            'duration' => null,
            'resolution' => null,
        ];
    }

    public function delete(User $user, int $id): array
    {
        $asset = MediaAsset::query()->where('user_id', $user->id)->find($id);
        if (! $asset) {
            throw new BusinessException('미디어를 찾을 수 없습니다.', 'MEDIA_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $asset->delete();

        return ['message' => '미디어가 삭제되었습니다.'];
    }

    private function find(int $id): MediaAsset
    {
        $asset = MediaAsset::query()->find($id);
        if (! $asset) {
            throw new BusinessException('미디어를 찾을 수 없습니다.', 'MEDIA_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $asset;
    }

    private function serialize(MediaAsset $asset): array
    {
        return [
            'id' => $asset->id,
            'ownerType' => $asset->owner_type,
            'ownerId' => $asset->owner_id,
            'fileName' => $asset->file_name,
            'fileUrl' => $asset->file_url,
            'mimeType' => $asset->mime_type,
            'size' => $asset->size,
            'createdAt' => optional($asset->created_at)?->toISOString(),
        ];
    }
}
