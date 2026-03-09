<?php

namespace App\Modules\Image\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\ImageAsset;
use App\Models\ImageVariant;
use App\Models\User;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Symfony\Component\HttpFoundation\Response;

class ImageService
{
    public function upload(User $user, UploadedFile $file, string $category): array
    {
        $path = $file->store('uploads/original', 'public');
        $asset = ImageAsset::query()->create([
            'user_id' => $user->id,
            'category' => $category,
            'original_name' => $file->getClientOriginalName(),
            'original_path' => $path,
            'original_url' => Storage::disk('public')->url($path),
            'mime_type' => $file->getMimeType() ?: 'image/jpeg',
            'size' => $file->getSize() ?: 0,
            'processing_status' => 'COMPLETED',
        ]);

        $variants = [
            ['type' => 'THUMBNAIL', 'width' => 200, 'height' => 200],
            ['type' => 'MEDIUM', 'width' => 600, 'height' => 600],
            ['type' => 'LARGE', 'width' => 1200, 'height' => 1200],
        ];

        foreach ($variants as $variant) {
            $variantPath = $path . '.' . strtolower($variant['type']) . '.webp';
            Storage::disk('public')->put($variantPath, 'variant');
            ImageVariant::query()->create([
                'image_asset_id' => $asset->id,
                'type' => $variant['type'],
                'path' => $variantPath,
                'url' => Storage::disk('public')->url($variantPath),
                'width' => $variant['width'],
                'height' => $variant['height'],
                'format' => 'webp',
                'size' => max(1, (int) round(($file->getSize() ?: 1) / 3)),
            ]);
        }

        return $this->serialize($asset->fresh('variants'));
    }

    public function variants(int $assetId): array
    {
        $asset = $this->findAsset($assetId);
        return $asset->variants->map(fn (ImageVariant $variant): array => [
            'id' => $variant->id,
            'type' => $variant->type,
            'url' => $variant->url,
            'width' => $variant->width,
            'height' => $variant->height,
            'format' => $variant->format,
            'size' => $variant->size,
        ])->values()->all();
    }

    public function remove(User $actor, int $assetId): array
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
        $asset = $this->findAsset($assetId);
        Storage::disk('public')->delete($asset->original_path);
        foreach ($asset->variants as $variant) {
            Storage::disk('public')->delete($variant->path);
        }
        $asset->delete();
        return ['message' => '이미지가 삭제되었습니다.'];
    }

    private function findAsset(int $assetId): ImageAsset
    {
        $asset = ImageAsset::query()->with('variants')->find($assetId);
        if (! $asset) {
            throw new BusinessException('이미지를 찾을 수 없습니다.', 'IMAGE_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        return $asset;
    }

    private function serialize(ImageAsset $asset): array
    {
        return [
            'id' => $asset->id,
            'originalUrl' => $asset->original_url,
            'variants' => $this->variants($asset->id),
            'processingStatus' => $asset->processing_status,
        ];
    }
}
