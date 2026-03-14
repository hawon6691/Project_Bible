<?php

namespace Tests\Support;

use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\TestCase as BaseTestCase;
use Illuminate\Support\Facades\Hash;

trait ApiAuthTestHelpers
{
    protected function createApiUser(string $role = 'USER'): User
    {
        return User::query()->create([
            'email' => uniqid('e2e-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'PB E2E User',
            'nickname' => uniqid('e2e-', false),
            'role' => $role,
            'status' => 'ACTIVE',
            'phone' => '01012345678',
            'email_verified_at' => now(),
        ]);
    }

    protected function actingAsApiUser(User $user): BaseTestCase
    {
        $token = app(JwtService::class)->createAccessToken($user);

        return $this->withHeader('Authorization', 'Bearer '.$token);
    }
}
