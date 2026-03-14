<?php

namespace Tests\E2E;

use App\Models\Category;
use App\Models\Product;
use App\Modules\Auth\Enums\AuthCodePurpose;
use App\Modules\Auth\Models\AuthCode;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class AuthSearchE2ETest extends TestCase
{
    use RefreshDatabase;

    public function test_signup_login_and_product_query_flow(): void
    {
        $category = Category::query()->create([
            'name' => '검색 카테고리',
            'slug' => 'search-category',
            'depth' => 0,
            'sort_order' => 1,
            'is_visible' => true,
        ]);

        Product::query()->create([
            'category_id' => $category->id,
            'name' => 'Search Laptop',
            'slug' => 'search-laptop',
            'status' => 'ACTIVE',
        ]);

        $this->postJson('/api/v1/auth/signup', [
            'email' => 'search-flow@example.com',
            'password' => 'Password123!',
            'name' => 'Search Flow',
            'nickname' => 'search-flow',
            'phone' => '01022223333',
        ])->assertCreated();

        AuthCode::query()
            ->where('email', 'search-flow@example.com')
            ->where('purpose', AuthCodePurpose::EMAIL_VERIFICATION)
            ->update([
                'code' => Hash::make('123456'),
                'expires_at' => now()->addMinutes(10),
            ]);

        $this->postJson('/api/v1/auth/verify-email', [
            'email' => 'search-flow@example.com',
            'code' => '123456',
        ])->assertOk();

        $login = $this->postJson('/api/v1/auth/login', [
            'email' => 'search-flow@example.com',
            'password' => 'Password123!',
        ]);

        $login->assertOk();
        $this->assertNotEmpty(data_get($login->json(), 'data.accessToken'));

        $this->getJson('/api/v1/products?search=Search')
            ->assertOk()
            ->assertJsonPath('data.items.0.name', 'Search Laptop');
    }
}
