<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class SecurityRegressionE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_security_sensitive_endpoints_enforce_authentication_and_authorization(): void
    {
        $user = $this->createApiUser('USER');

        $this->getJson('/api/v1/users/me')
            ->assertUnauthorized();

        $this->withHeader('Authorization', 'Bearer invalid-token')
            ->getJson('/api/v1/users/me')
            ->assertUnauthorized();

        $this->actingAsApiUser($user)
            ->getJson('/api/v1/admin/queues/supported')
            ->assertForbidden()
            ->assertJsonPath('error.code', 'FORBIDDEN');
    }
}
