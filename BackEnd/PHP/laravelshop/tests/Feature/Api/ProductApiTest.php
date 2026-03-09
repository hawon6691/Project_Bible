<?php

namespace Tests\Feature\Api;

use App\Models\Category;
use App\Models\PriceEntry;
use App\Models\Product;
use App\Models\ProductSpec;
use App\Models\Seller;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class ProductApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_public_can_list_and_view_products(): void
    {
        $category = Category::query()->create([
            'name' => '노트북',
            'slug' => 'laptop',
            'depth' => 0,
            'sort_order' => 1,
            'is_visible' => true,
        ]);

        $product = Product::query()->create([
            'category_id' => $category->id,
            'name' => 'PB Laptop Pro',
            'slug' => 'pb-laptop-pro',
            'description' => '고성능 노트북',
            'brand' => 'PB',
            'status' => 'ACTIVE',
            'thumbnail_url' => '/uploads/products/pb-laptop-pro.jpg',
            'rating_avg' => 4.5,
            'review_count' => 8,
        ]);

        ProductSpec::query()->create([
            'product_id' => $product->id,
            'spec_key' => 'CPU',
            'spec_value' => 'Ryzen 7',
            'sort_order' => 1,
        ]);

        $seller = Seller::query()->create([
            'name' => 'PB Seller',
            'code' => 'pb-seller',
            'status' => 'ACTIVE',
            'rating' => 4.8,
        ]);

        PriceEntry::query()->create([
            'product_id' => $product->id,
            'seller_id' => $seller->id,
            'price' => 1500000,
            'shipping_fee' => 0,
            'stock_status' => 'IN_STOCK',
            'collected_at' => now(),
        ]);

        $listResponse = $this->getJson('/api/v1/products?search=Laptop&categoryId='.$category->id.'&sort=price_asc');
        $listResponse->assertOk();
        $listResponse->assertJsonPath('data.items.0.name', 'PB Laptop Pro');
        $this->assertEquals(1500000.0, $listResponse->json('data.items.0.lowestPrice'));
        $listResponse->assertJsonPath('data.pagination.totalCount', 1);

        $detailResponse = $this->getJson('/api/v1/products/'.$product->id);
        $detailResponse->assertOk();
        $detailResponse->assertJsonPath('data.name', 'PB Laptop Pro');
        $detailResponse->assertJsonPath('data.category.name', '노트북');
        $detailResponse->assertJsonPath('data.specs.0.name', 'CPU');
        $detailResponse->assertJsonPath('data.priceEntries.0.seller.name', 'PB Seller');
    }

    public function test_admin_can_manage_products_and_non_admin_is_forbidden(): void
    {
        $category = Category::query()->create([
            'name' => '태블릿',
            'slug' => 'tablet',
            'depth' => 0,
            'sort_order' => 1,
            'is_visible' => true,
        ]);

        $admin = $this->createUser('ADMIN');
        $member = $this->createUser('USER');

        $createResponse = $this->actingAsApiUser($admin)->postJson('/api/v1/products', [
            'categoryId' => $category->id,
            'name' => 'PB Tablet',
            'description' => '태블릿 설명',
            'brand' => 'PB',
            'status' => 'ACTIVE',
            'thumbnailUrl' => '/uploads/products/pb-tablet.jpg',
        ]);

        $createResponse->assertCreated();
        $productId = $createResponse->json('data.id');
        $createResponse->assertJsonPath('data.name', 'PB Tablet');

        $updateResponse = $this->actingAsApiUser($admin)->patchJson('/api/v1/products/'.$productId, [
            'name' => 'PB Tablet 2',
            'brand' => 'PB Updated',
            'status' => 'INACTIVE',
        ]);

        $updateResponse->assertOk();
        $updateResponse->assertJsonPath('data.name', 'PB Tablet 2');
        $updateResponse->assertJsonPath('data.brand', 'PB Updated');
        $updateResponse->assertJsonPath('data.status', 'INACTIVE');

        $forbiddenResponse = $this->actingAsApiUser($member)->postJson('/api/v1/products', [
            'categoryId' => $category->id,
            'name' => 'Forbidden Product',
        ]);

        $forbiddenResponse->assertForbidden();
        $forbiddenResponse->assertJsonPath('error.code', 'FORBIDDEN');

        $deleteResponse = $this->actingAsApiUser($admin)->deleteJson('/api/v1/products/'.$productId);
        $deleteResponse->assertOk();
        $deleteResponse->assertJsonPath('data.message', '상품이 삭제되었습니다.');
        $this->assertDatabaseMissing('products', ['id' => $productId]);
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('product-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Product User',
            'nickname' => 'product-user',
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
