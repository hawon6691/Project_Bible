<?php

namespace Tests\Feature\Api;

use Tests\TestCase;

class HealthRouteTest extends TestCase
{
    public function test_health_route_returns_standard_api_contract(): void
    {
        $response = $this
            ->withHeaders([
                'Accept-Language' => 'ja',
                'X-Currency' => 'USD',
                'X-Request-Id' => 'health-test-request',
            ])
            ->getJson('/api/v1/health');

        $response
            ->assertOk()
            ->assertHeader('Content-Language', 'ja')
            ->assertHeader('X-Currency', 'USD')
            ->assertHeader('X-Request-Id', 'health-test-request')
            ->assertJsonPath('success', true)
            ->assertJsonPath('data.service', 'PBShop')
            ->assertJsonPath('data.language', 'php')
            ->assertJsonPath('data.framework', 'laravel')
            ->assertJsonPath('meta.requestId', 'health-test-request')
            ->assertJsonPath('meta.locale', 'ja')
            ->assertJsonPath('meta.currency', 'USD');
    }
}
