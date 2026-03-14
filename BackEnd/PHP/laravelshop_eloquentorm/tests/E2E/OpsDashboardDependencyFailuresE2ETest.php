<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class OpsDashboardDependencyFailuresE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_ops_dashboard_exposes_degraded_state_when_dependency_checks_are_observed(): void
    {
        $admin = $this->createApiUser('ADMIN');

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/ops-dashboard/summary')
            ->assertOk()
            ->assertJsonPath('data.overallStatus', 'degraded');

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/observability/dashboard')
            ->assertOk()
            ->assertJsonPath('data.opsSummary.overallStatus', 'degraded');
    }
}
