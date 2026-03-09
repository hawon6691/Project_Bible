<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class ObservabilityE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_observability_endpoints_are_available(): void
    {
        $admin = $this->createApiUser('ADMIN');

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/observability/metrics')
            ->assertOk()
            ->assertJsonPath('success', true);

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/observability/dashboard')
            ->assertOk()
            ->assertJsonPath('success', true);
    }
}
