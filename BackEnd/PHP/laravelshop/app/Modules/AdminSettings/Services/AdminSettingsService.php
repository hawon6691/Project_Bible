<?php

namespace App\Modules\AdminSettings\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\SystemSetting;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class AdminSettingsService
{
    public function extensions(User $user): array
    {
        $this->assertAdmin($user);
        return ['extensions' => $this->get('extensions', ['jpg', 'png', 'mp4', 'mp3'])];
    }

    public function updateExtensions(User $user, array $payload): array
    {
        $this->assertAdmin($user);
        $this->put('extensions', $payload['extensions']);
        return ['extensions' => $payload['extensions']];
    }

    public function uploadLimits(User $user): array
    {
        $this->assertAdmin($user);
        return $this->get('upload_limits', ['image' => 5, 'video' => 100, 'audio' => 20]);
    }

    public function updateUploadLimits(User $user, array $payload): array
    {
        $this->assertAdmin($user);
        $this->put('upload_limits', $payload);
        return $payload;
    }

    public function reviewPolicy(User $user): array
    {
        $this->assertAdmin($user);
        return $this->get('review_policy', ['maxImageCount' => 10, 'pointAmount' => 500]);
    }

    public function updateReviewPolicy(User $user, array $payload): array
    {
        $this->assertAdmin($user);
        $this->put('review_policy', $payload);
        return $payload;
    }

    private function get(string $key, array $default): array
    {
        $setting = SystemSetting::query()->where('setting_key', $key)->first();
        return is_array($setting?->setting_value) ? $setting->setting_value : $default;
    }

    private function put(string $key, array $value): void
    {
        SystemSetting::query()->updateOrCreate(['setting_key' => $key], ['setting_value' => $value]);
    }

    private function assertAdmin(User $user): void
    {
        if ($user->role !== 'ADMIN') {
            throw new BusinessException('관리자 권한이 필요합니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }
    }
}
