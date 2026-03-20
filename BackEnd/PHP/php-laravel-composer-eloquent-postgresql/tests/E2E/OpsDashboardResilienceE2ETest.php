<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class OpsDashboardResilienceE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_ops_dashboard_returns_degraded_summary_with_resilience_signals(): void
    {
        $admin = $this->createApiUser('ADMIN');

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/ops-dashboard/summary')
            ->assertOk()
            ->assertJsonPath('data.overallStatus', 'degraded')
            ->assertJsonPath('data.health.status', 'ok')
            ->assertJsonPath('data.alertCount', 1);

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/resilience/circuit-breakers')
            ->assertOk()
            ->assertJsonPath('data.items.1.name', 'crawler')
            ->assertJsonPath('data.items.1.state', 'HALF_OPEN');
    }
}
