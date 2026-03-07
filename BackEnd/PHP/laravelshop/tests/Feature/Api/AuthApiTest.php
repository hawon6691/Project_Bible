<?php

namespace Tests\Feature\Api;

use App\Models\User;
use App\Modules\Auth\Enums\AuthCodePurpose;
use App\Modules\Auth\Models\AuthCode;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class AuthApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_signup_creates_user_and_verification_code(): void
    {
        $signupResponse = $this->postJson('/api/v1/auth/signup', [
            'email' => 'auth-flow@example.com',
            'password' => 'Password123!',
            'name' => 'Auth Flow',
            'nickname' => 'auth-flow',
            'phone' => '01012341234',
        ]);

        $signupResponse->assertCreated();
        $signupResponse->assertJsonPath('success', true);

        $user = User::query()->where('email', 'auth-flow@example.com')->firstOrFail();
        $this->assertNull($user->email_verified_at);
        $this->assertDatabaseHas('auth_codes', [
            'email' => $user->email,
            'purpose' => AuthCodePurpose::EMAIL_VERIFICATION,
        ]);
    }

    public function test_verify_login_refresh_and_logout_flow(): void
    {
        $user = User::query()->create([
            'email' => 'verified-flow@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Verified Flow',
            'phone' => '01099998888',
            'role' => 'USER',
            'status' => 'ACTIVE',
        ]);

        AuthCode::query()->create([
            'user_id' => $user->id,
            'email' => $user->email,
            'purpose' => AuthCodePurpose::EMAIL_VERIFICATION,
            'code' => Hash::make('123456'),
            'expires_at' => now()->addMinutes(10),
        ]);

        $verifyResponse = $this->postJson('/api/v1/auth/verify-email', [
            'email' => $user->email,
            'code' => '123456',
        ]);

        $verifyResponse->assertOk();
        $verifyResponse->assertJsonPath('success', true);

        $user->refresh();
        $this->assertNotNull($user->email_verified_at);

        $loginResponse = $this->postJson('/api/v1/auth/login', [
            'email' => $user->email,
            'password' => 'Password123!',
        ]);

        $loginResponse->assertOk();
        $loginResponse->assertJsonPath('success', true);

        $loginPayload = $loginResponse->json();
        $accessToken = $this->extractValue($loginPayload, [
            'data.accessToken',
            'data.access_token',
            'accessToken',
            'access_token',
        ]);
        $refreshToken = $this->extractValue($loginPayload, [
            'data.refreshToken',
            'data.refresh_token',
            'refreshToken',
            'refresh_token',
        ]);

        $this->assertIsString($accessToken);
        $this->assertIsString($refreshToken);
        $this->assertNotEmpty($accessToken);
        $this->assertNotEmpty($refreshToken);

        $refreshResponse = $this->postJson('/api/v1/auth/refresh', [
            'refreshToken' => $refreshToken,
        ]);

        $refreshResponse->assertOk();
        $refreshResponse->assertJsonPath('success', true);

        $refreshPayload = $refreshResponse->json();
        $refreshedAccessToken = $this->extractValue($refreshPayload, [
            'data.accessToken',
            'data.access_token',
            'accessToken',
            'access_token',
        ]);

        $this->assertIsString($refreshedAccessToken);
        $this->assertNotEmpty($refreshedAccessToken);

        $logoutResponse = $this
            ->withHeader('Authorization', 'Bearer ' . $refreshedAccessToken)
            ->postJson('/api/v1/auth/logout');

        $logoutResponse->assertOk();
        $logoutResponse->assertJsonPath('success', true);
    }

    /**
     * @param  array<int, string>  $paths
     */
    private function extractValue(array $payload, array $paths): mixed
    {
        foreach ($paths as $path) {
            $value = data_get($payload, $path);

            if ($value !== null && $value !== '') {
                return $value;
            }
        }

        return null;
    }
}
