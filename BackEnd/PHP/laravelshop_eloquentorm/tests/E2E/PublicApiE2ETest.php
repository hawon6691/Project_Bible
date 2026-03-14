<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class PublicApiE2ETest extends TestCase
{
    use RefreshDatabase;

    public function test_public_api_endpoints_return_wrapped_success_responses(): void
    {
        $this->getJson('/api/v1/health')
            ->assertOk()
            ->assertJsonPath('success', true)
            ->assertJsonPath('data.language', 'php');

        $this->getJson('/api/v1/errors/codes')
            ->assertOk()
            ->assertJsonPath('success', true)
            ->assertJsonPath('data.total', 3);
    }
}
