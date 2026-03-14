<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class AdminAuthorizationBoundaryE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_non_admin_cannot_access_admin_platform_endpoints(): void
    {
        $user = $this->createApiUser('USER');

        $this->actingAsApiUser($user)
            ->getJson('/api/v1/admin/queues/supported')
            ->assertForbidden()
            ->assertJsonPath('error.code', 'FORBIDDEN');

        $this->actingAsApiUser($user)
            ->getJson('/api/v1/admin/ops-dashboard/summary')
            ->assertForbidden()
            ->assertJsonPath('error.code', 'FORBIDDEN');
    }
}
