<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class OpsDashboardE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_ops_dashboard_and_resilience_endpoints_are_available(): void
    {
        $admin = $this->createApiUser('ADMIN');

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/ops-dashboard/summary')
            ->assertOk()
            ->assertJsonPath('success', true);

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/resilience/circuit-breakers')
            ->assertOk()
            ->assertJsonPath('success', true);
    }
}
