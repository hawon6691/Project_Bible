<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class OpsDashboardThresholdsE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_ops_dashboard_exposes_alert_threshold_related_fields(): void
    {
        $admin = $this->createApiUser('ADMIN');

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/ops-dashboard/summary')
            ->assertOk()
            ->assertJsonPath('data.alertCount', 1)
            ->assertJsonPath('data.alerts.0', 'crawler queue has 1 failed job');

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/observability/dashboard')
            ->assertOk()
            ->assertJsonPath('data.opsSummary.overallStatus', 'degraded');
    }
}
