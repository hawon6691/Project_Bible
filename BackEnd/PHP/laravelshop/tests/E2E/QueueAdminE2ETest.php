<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class QueueAdminE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_queue_admin_endpoints_are_available(): void
    {
        $admin = $this->createApiUser('ADMIN');

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/queues/supported')
            ->assertOk()
            ->assertJsonPath('success', true);

        $this->actingAsApiUser($admin)
            ->getJson('/api/v1/admin/queues/stats')
            ->assertOk()
            ->assertJsonPath('success', true);
    }
}
