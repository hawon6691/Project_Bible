<?php

namespace Tests\E2E;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\Support\ApiAuthTestHelpers;
use Tests\TestCase;

class AdminPlatformE2ETest extends TestCase
{
    use ApiAuthTestHelpers;
    use RefreshDatabase;

    public function test_admin_platform_endpoints_are_available(): void
    {
        $admin = $this->createApiUser('ADMIN');

        $this->actingAsApiUser($admin)
            ->postJson('/api/v1/admin/settings/extensions', ['extensions' => ['jpg', 'png']])
            ->assertOk()
            ->assertJsonPath('success', true);

        $this->actingAsApiUser($admin)
            ->patchJson('/api/v1/admin/settings/upload-limits', ['image' => 10, 'video' => 100, 'audio' => 20])
            ->assertOk()
            ->assertJsonPath('success', true);
    }
}
