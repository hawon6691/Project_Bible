<?php

namespace Tests\Feature\Api;

use App\Models\Category;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class CategoryApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_public_can_fetch_category_tree_and_single_category(): void
    {
        $root = Category::query()->create([
            'name' => '컴퓨터',
            'slug' => 'computer',
            'depth' => 0,
            'sort_order' => 1,
            'is_visible' => true,
        ]);

        $child = Category::query()->create([
            'parent_id' => $root->id,
            'name' => '노트북',
            'slug' => 'laptop',
            'depth' => 1,
            'sort_order' => 2,
            'is_visible' => true,
        ]);

        $treeResponse = $this->getJson('/api/v1/categories');
        $treeResponse->assertOk();
        $treeResponse->assertJsonPath('data.0.name', '컴퓨터');
        $treeResponse->assertJsonPath('data.0.children.0.name', '노트북');

        $showResponse = $this->getJson('/api/v1/categories/'.$child->id);
        $showResponse->assertOk();
        $showResponse->assertJsonPath('data.name', '노트북');
        $showResponse->assertJsonPath('data.parent.name', '컴퓨터');
    }

    public function test_admin_can_manage_categories_and_non_admin_is_forbidden(): void
    {
        $admin = $this->createUser('ADMIN');
        $member = $this->createUser('USER');

        $createResponse = $this->actingAsApiUser($admin)->postJson('/api/v1/categories', [
            'name' => '가전/TV',
            'sortOrder' => 10,
            'isVisible' => true,
        ]);

        $createResponse->assertCreated();
        $categoryId = $createResponse->json('data.id');
        $this->assertIsString($createResponse->json('data.slug'));
        $this->assertNotEmpty($createResponse->json('data.slug'));

        $updateResponse = $this->actingAsApiUser($admin)->patchJson('/api/v1/categories/'.$categoryId, [
            'name' => '가전 및 TV',
            'sortOrder' => 20,
            'isVisible' => false,
        ]);

        $updateResponse->assertOk();
        $updateResponse->assertJsonPath('data.name', '가전 및 TV');
        $updateResponse->assertJsonPath('data.sortOrder', 20);
        $updateResponse->assertJsonPath('data.isVisible', false);

        $forbiddenResponse = $this->actingAsApiUser($member)->postJson('/api/v1/categories', [
            'name' => '비허용 카테고리',
        ]);

        $forbiddenResponse->assertForbidden();
        $forbiddenResponse->assertJsonPath('error.code', 'FORBIDDEN');

        $deleteResponse = $this->actingAsApiUser($admin)->deleteJson('/api/v1/categories/'.$categoryId);
        $deleteResponse->assertOk();
        $deleteResponse->assertJsonPath('data.message', '카테고리가 삭제되었습니다.');
        $this->assertDatabaseMissing('categories', ['id' => $categoryId]);
    }

    public function test_admin_cannot_delete_category_with_children(): void
    {
        $admin = $this->createUser('ADMIN');

        $parent = Category::query()->create([
            'name' => '컴퓨터',
            'slug' => 'computer',
            'depth' => 0,
            'sort_order' => 0,
            'is_visible' => true,
        ]);

        Category::query()->create([
            'parent_id' => $parent->id,
            'name' => '노트북',
            'slug' => 'laptop',
            'depth' => 1,
            'sort_order' => 0,
            'is_visible' => true,
        ]);

        $response = $this->actingAsApiUser($admin)->deleteJson('/api/v1/categories/'.$parent->id);
        $response->assertBadRequest();
        $response->assertJsonPath('error.code', 'CATEGORY_HAS_CHILDREN');
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('category-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Category User',
            'nickname' => 'category-user',
            'role' => $role,
            'status' => 'ACTIVE',
            'phone' => '01012345678',
            'email_verified_at' => now(),
        ]);
    }

    private function actingAsApiUser(User $user): self
    {
        $token = app(JwtService::class)->createAccessToken($user);

        return $this->withHeader('Authorization', 'Bearer '.$token);
    }
}
