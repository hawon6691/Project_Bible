<?php

namespace Tests\Feature\Api;

use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

class UserApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_get_update_profile_image_and_delete_me(): void
    {
        Storage::fake('public');

        $user = $this->createUser([
            'email' => 'user-api@example.com',
            'role' => 'USER',
            'status' => 'ACTIVE',
        ]);

        $response = $this->actingAsApiUser($user)->getJson('/api/v1/users/me');
        $response->assertOk();
        $response->assertJsonPath('data.email', 'user-api@example.com');

        $updateResponse = $this->actingAsApiUser($user)->patchJson('/api/v1/users/me', [
            'name' => 'Updated User',
            'phone' => '01011112222',
            'password' => 'NewPassword123!',
        ]);

        $updateResponse->assertOk();
        $updateResponse->assertJsonPath('data.name', 'Updated User');
        $updateResponse->assertJsonPath('data.phone', '01011112222');

        $profileResponse = $this->actingAsApiUser($user->fresh())->patchJson('/api/v1/users/me/profile', [
            'nickname' => 'updated-nickname',
            'bio' => 'updated bio',
        ]);

        $profileResponse->assertOk();
        $profileResponse->assertJsonPath('data.nickname', 'updated-nickname');
        $profileResponse->assertJsonPath('data.bio', 'updated bio');

        $uploadResponse = $this
            ->actingAsApiUser($user->fresh())
            ->post('/api/v1/users/me/profile-image', [
                'image' => $this->makePngUpload(),
            ], [
                'Accept' => 'application/json',
            ]);

        $uploadResponse->assertOk();
        $imageUrl = $uploadResponse->json('data.imageUrl');
        $this->assertIsString($imageUrl);
        $this->assertNotEmpty($imageUrl);
        Storage::disk('public')->assertExists('profile-images/'.basename($imageUrl));

        $deleteImageResponse = $this->actingAsApiUser($user->fresh())->deleteJson('/api/v1/users/me/profile-image');
        $deleteImageResponse->assertOk();
        $deleteImageResponse->assertJsonPath('data.message', '프로필 이미지가 삭제되었습니다.');

        $deleteResponse = $this->actingAsApiUser($user->fresh())->deleteJson('/api/v1/users/me');
        $deleteResponse->assertOk();
        $deleteResponse->assertJsonPath('data.message', '회원 탈퇴가 완료되었습니다.');
        $this->assertDatabaseMissing('users', ['id' => $user->id]);
    }

    public function test_public_profile_and_admin_user_management(): void
    {
        $publicUser = $this->createUser([
            'email' => 'public-profile@example.com',
            'name' => 'Public Profile',
            'nickname' => 'public-user',
            'bio' => 'profile bio',
            'role' => 'USER',
        ]);

        $profileResponse = $this->getJson('/api/v1/users/'.$publicUser->id.'/profile');
        $profileResponse->assertOk();
        $profileResponse->assertJsonPath('data.nickname', 'public-user');
        $profileResponse->assertJsonMissingPath('data.email');

        $admin = $this->createUser([
            'email' => 'admin-user@example.com',
            'role' => 'ADMIN',
            'status' => 'ACTIVE',
        ]);

        $memberA = $this->createUser([
            'email' => 'member-a@example.com',
            'name' => 'Member Alpha',
            'status' => 'ACTIVE',
        ]);

        $memberB = $this->createUser([
            'email' => 'member-b@example.com',
            'name' => 'Member Beta',
            'status' => 'BLOCKED',
        ]);

        $listResponse = $this->actingAsApiUser($admin)->getJson('/api/v1/users?search=Member&status=ACTIVE');
        $listResponse->assertOk();
        $listResponse->assertJsonPath('data.pagination.total', 1);
        $listResponse->assertJsonPath('data.items.0.email', 'member-a@example.com');

        $statusResponse = $this->actingAsApiUser($admin)->patchJson('/api/v1/users/'.$memberA->id.'/status', [
            'status' => 'BLOCKED',
        ]);

        $statusResponse->assertOk();
        $statusResponse->assertJsonPath('data.status', 'BLOCKED');

        $forbiddenResponse = $this->actingAsApiUser($memberB)->getJson('/api/v1/users');
        $forbiddenResponse->assertForbidden();
        $forbiddenResponse->assertJsonPath('success', false);
        $forbiddenResponse->assertJsonPath('error.code', 'FORBIDDEN');
    }

    private function createUser(array $overrides = []): User
    {
        return User::query()->create(array_merge([
            'email' => 'user-'.uniqid().'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'PBShop User',
            'nickname' => 'pbshop-user',
            'role' => 'USER',
            'status' => 'ACTIVE',
            'phone' => '01000000000',
            'email_verified_at' => now(),
        ], $overrides));
    }

    private function actingAsApiUser(User $user): self
    {
        $token = app(JwtService::class)->createAccessToken($user);

        return $this->withHeader('Authorization', 'Bearer '.$token);
    }

    private function makePngUpload(): UploadedFile
    {
        $path = tempnam(sys_get_temp_dir(), 'pbshop-profile-');
        $png = base64_decode(
            'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9pGdb9sAAAAASUVORK5CYII=',
            true
        );

        file_put_contents($path, $png);

        return new UploadedFile(
            $path,
            'profile.png',
            'image/png',
            null,
            true
        );
    }
}
