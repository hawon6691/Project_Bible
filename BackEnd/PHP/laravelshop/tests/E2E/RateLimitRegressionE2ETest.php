<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class RateLimitRegressionE2ETest extends TestCase
{
    use RefreshDatabase;

    public function test_repeated_health_requests_are_stable(): void
    {
        for ($i = 0; $i < 30; $i++) {
            $this->getJson('/api/v1/health')
                ->assertOk()
                ->assertJsonPath('success', true);
        }

        $this->assertTrue(true);
    }
}
