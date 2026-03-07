<?php

namespace App\Modules\Auth\Services;

use App\Models\User;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;

class JwtService
{
    public function createAccessToken(User $user): string
    {
        return $this->encode([
            'sub' => $user->id,
            'email' => $user->email,
            'role' => $user->role,
            'type' => 'access',
        ], config('pbshop.auth.access_ttl', 3600));
    }

    public function decode(string $token): object
    {
        return JWT::decode($token, new Key($this->secret(), 'HS256'));
    }

    private function encode(array $claims, int $ttlSeconds): string
    {
        $now = time();

        return JWT::encode(array_merge($claims, [
            'iss' => config('app.url'),
            'iat' => $now,
            'nbf' => $now,
            'exp' => $now + $ttlSeconds,
        ]), $this->secret(), 'HS256');
    }

    private function secret(): string
    {
        return (string) config('pbshop.auth.jwt_secret');
    }
}
