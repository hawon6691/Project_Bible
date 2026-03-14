<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class ResilienceAutoTuneE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_resilience_policies_endpoint_exposes_tuned_policy_snapshot(): void
    {
        $admin = $this->createApiUser('ADMIN');

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/resilience/circuit-breakers/policies')
            ->assertOk()
            ->assertJsonPath('data.items.0.name', 'crawler')
            ->assertJsonPath('data.items.0.options.threshold', 5)
            ->assertJsonPath('data.items.0.stats.opened', 1);
    }
}
