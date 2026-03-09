<?php

namespace Tests\Feature\Api;

use App\Models\CrawlerJob;
use App\Models\Category;
use App\Models\Product;
use App\Models\SearchIndexOutbox;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class PhpOpsApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_admin_settings_health_error_codes_and_resilience(): void
    {
        $admin = $this->createUser('ADMIN');

        $this->getJson('/api/v1/health')->assertOk()->assertJsonPath('data.language', 'php');
        $this->getJson('/api/v1/errors/codes')->assertOk()->assertJsonPath('data.total', 3);
        $this->actingAsApiUser($admin)->postJson('/api/v1/admin/settings/extensions', ['extensions' => ['jpg', 'png']])->assertOk()->assertJsonPath('data.extensions.0', 'jpg');
        $this->actingAsApiUser($admin)->getJson('/api/v1/resilience/circuit-breakers')->assertOk()->assertJsonPath('data.items.0.name', 'search-sync');
        $this->actingAsApiUser($admin)->postJson('/api/v1/resilience/circuit-breakers/crawler/reset')->assertOk()->assertJsonPath('data.name', 'crawler');
    }

    public function test_queue_query_searchsync_and_crawler_flow(): void
    {
        $admin = $this->createUser('ADMIN');
        $category = Category::query()->create(['name' => 'Ops', 'slug' => 'ops-' . uniqid(), 'depth' => 0, 'sort_order' => 0, 'is_visible' => true]);
        $product = Product::query()->create(['category_id' => $category->id, 'name' => 'PB Ops Product', 'slug' => 'pb-ops-' . uniqid(), 'status' => 'ACTIVE']);
        SearchIndexOutbox::query()->create(['entity_type' => 'PRODUCT', 'entity_id' => $product->id, 'status' => 'FAILED']);

        $this->actingAsApiUser($admin)->getJson('/api/v1/admin/queues/supported')->assertOk()->assertJsonPath('data.items.0', 'default');
        $this->actingAsApiUser($admin)->postJson('/api/v1/admin/query/products/' . $product->id . '/sync')->assertOk()->assertJsonPath('data.productId', $product->id);
        $this->actingAsApiUser($admin)->getJson('/api/v1/query/products/' . $product->id)->assertOk()->assertJsonPath('data.id', $product->id);
        $this->actingAsApiUser($admin)->getJson('/api/v1/search/admin/index/outbox/summary')->assertOk()->assertJsonPath('data.failed', 1);
        $this->actingAsApiUser($admin)->postJson('/api/v1/search/admin/index/outbox/requeue-failed?limit=5')->assertOk()->assertJsonPath('data.requeuedCount', 1);

        $job = $this->actingAsApiUser($admin)->postJson('/api/v1/crawler/admin/jobs', [
            'name' => 'PB 크롤러',
            'jobType' => 'PRODUCT',
            'status' => 'ACTIVE',
            'payload' => ['seller' => 'PB'],
        ]);
        $job->assertCreated();
        $jobId = $job->json('data.id');
        $this->actingAsApiUser($admin)->postJson('/api/v1/crawler/admin/jobs/' . $jobId . '/run')->assertOk()->assertJsonStructure(['data' => ['runId']]);
        $this->actingAsApiUser($admin)->getJson('/api/v1/crawler/admin/monitoring')->assertOk()->assertJsonPath('data.jobCount', 1);
    }

    public function test_ops_dashboard_and_observability(): void
    {
        $admin = $this->createUser('ADMIN');

        $this->actingAsApiUser($admin)->getJson('/api/v1/admin/ops-dashboard/summary')->assertOk()->assertJsonPath('data.overallStatus', 'degraded');
        $this->actingAsApiUser($admin)->getJson('/api/v1/admin/observability/metrics')->assertOk()->assertJsonPath('data.totalRequests', 120);
        $this->actingAsApiUser($admin)->getJson('/api/v1/admin/observability/traces?limit=5&pathContains=/api/v1/health')->assertOk()->assertJsonPath('data.items.0.path', '/api/v1/health');
        $this->actingAsApiUser($admin)->getJson('/api/v1/admin/observability/dashboard')->assertOk()->assertJsonPath('data.opsSummary.overallStatus', 'degraded');
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('ops-', true) . '@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'PB Ops User',
            'nickname' => uniqid('ops-', false),
            'role' => $role,
            'status' => 'ACTIVE',
            'phone' => '01012345678',
            'email_verified_at' => now(),
        ]);
    }

    private function actingAsApiUser(User $user): self
    {
        $token = app(JwtService::class)->createAccessToken($user);
        return $this->withHeader('Authorization', 'Bearer ' . $token);
    }
}
