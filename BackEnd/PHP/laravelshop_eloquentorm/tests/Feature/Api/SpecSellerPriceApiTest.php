<?php

namespace Tests\Feature\Api;

use App\Models\Category;
use App\Models\Product;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class SpecSellerPriceApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_admin_can_manage_spec_definitions_and_product_specs(): void
    {
        $admin = $this->createUser('ADMIN');
        $category = Category::query()->create([
            'name' => '노트북',
            'slug' => 'laptop',
            'depth' => 0,
            'sort_order' => 0,
            'is_visible' => true,
        ]);
        $product = Product::query()->create([
            'category_id' => $category->id,
            'name' => 'PB Laptop',
            'slug' => 'pb-laptop',
            'status' => 'ACTIVE',
        ]);

        $definitionResponse = $this->actingAsApiUser($admin)->postJson('/api/v1/specs/definitions', [
            'categoryId' => $category->id,
            'name' => 'CPU',
            'type' => 'SELECT',
            'options' => ['i5', 'i7'],
            'sortOrder' => 1,
        ]);
        $definitionResponse->assertCreated();
        $definitionId = $definitionResponse->json('data.id');

        $listResponse = $this->getJson('/api/v1/specs/definitions?categoryId='.$category->id);
        $listResponse->assertOk();
        $listResponse->assertJsonPath('data.0.name', 'CPU');

        $setSpecsResponse = $this->actingAsApiUser($admin)->putJson('/api/v1/products/'.$product->id.'/specs', [
            'specs' => [
                ['name' => 'CPU', 'value' => 'i7', 'sortOrder' => 1],
                ['name' => 'RAM', 'value' => '16GB', 'sortOrder' => 2],
            ],
        ]);
        $setSpecsResponse->assertOk();
        $setSpecsResponse->assertJsonPath('data.0.name', 'CPU');

        $getSpecsResponse = $this->getJson('/api/v1/products/'.$product->id.'/specs');
        $getSpecsResponse->assertOk();
        $getSpecsResponse->assertJsonPath('data.1.value', '16GB');

        $updateDefinitionResponse = $this->actingAsApiUser($admin)->patchJson('/api/v1/specs/definitions/'.$definitionId, [
            'name' => 'Processor',
        ]);
        $updateDefinitionResponse->assertOk();
        $updateDefinitionResponse->assertJsonPath('data.name', 'Processor');

        $deleteDefinitionResponse = $this->actingAsApiUser($admin)->deleteJson('/api/v1/specs/definitions/'.$definitionId);
        $deleteDefinitionResponse->assertOk();
        $deleteDefinitionResponse->assertJsonPath('data.message', '스펙 정의가 삭제되었습니다.');
    }

    public function test_admin_can_manage_sellers_and_prices_and_user_can_manage_alerts(): void
    {
        $admin = $this->createUser('ADMIN');
        $sellerUser = $this->createUser('SELLER');
        $member = $this->createUser('USER');
        $category = Category::query()->create([
            'name' => '태블릿',
            'slug' => 'tablet',
            'depth' => 0,
            'sort_order' => 0,
            'is_visible' => true,
        ]);
        $product = Product::query()->create([
            'category_id' => $category->id,
            'name' => 'PB Tablet',
            'slug' => 'pb-tablet',
            'status' => 'ACTIVE',
        ]);

        $sellerResponse = $this->actingAsApiUser($admin)->postJson('/api/v1/sellers', [
            'name' => 'PB Mall',
            'code' => 'pb-mall',
            'status' => 'ACTIVE',
            'rating' => 4.7,
            'contactEmail' => 'seller@example.com',
            'homepageUrl' => 'https://seller.example.com',
        ]);
        $sellerResponse->assertCreated();
        $sellerId = $sellerResponse->json('data.id');

        $listSellers = $this->getJson('/api/v1/sellers');
        $listSellers->assertOk();
        $listSellers->assertJsonPath('data.items.0.name', 'PB Mall');

        $createPrice = $this->actingAsApiUser($sellerUser)->postJson('/api/v1/products/'.$product->id.'/prices', [
            'sellerId' => $sellerId,
            'price' => 999000,
            'shippingFee' => 0,
            'stockStatus' => 'IN_STOCK',
            'isCardDiscount' => true,
        ]);
        $createPrice->assertCreated();
        $priceId = $createPrice->json('data.id');

        $pricesResponse = $this->getJson('/api/v1/products/'.$product->id.'/prices');
        $pricesResponse->assertOk();
        $this->assertEquals(999000.0, $pricesResponse->json('data.lowestPrice'));

        $historyResponse = $this->getJson('/api/v1/products/'.$product->id.'/price-history');
        $historyResponse->assertOk();
        $historyResponse->assertJsonPath('data.productId', $product->id);

        $updatePrice = $this->actingAsApiUser($admin)->patchJson('/api/v1/prices/'.$priceId, [
            'price' => 979000,
            'shippingFee' => 2500,
        ]);
        $updatePrice->assertOk();
        $this->assertEquals(979000.0, $updatePrice->json('data.price'));

        $alertResponse = $this->actingAsApiUser($member)->postJson('/api/v1/price-alerts', [
            'productId' => $product->id,
            'targetPrice' => 950000,
        ]);
        $alertResponse->assertCreated();
        $alertId = $alertResponse->json('data.id');

        $listAlerts = $this->actingAsApiUser($member)->getJson('/api/v1/price-alerts');
        $listAlerts->assertOk();
        $listAlerts->assertJsonPath('data.0.productName', 'PB Tablet');

        $deleteAlert = $this->actingAsApiUser($member)->deleteJson('/api/v1/price-alerts/'.$alertId);
        $deleteAlert->assertOk();

        $forbiddenDelete = $this->actingAsApiUser($member)->deleteJson('/api/v1/prices/'.$priceId);
        $forbiddenDelete->assertForbidden();
        $forbiddenDelete->assertJsonPath('error.code', 'FORBIDDEN');

        $deletePrice = $this->actingAsApiUser($admin)->deleteJson('/api/v1/prices/'.$priceId);
        $deletePrice->assertOk();
        $deletePrice->assertJsonPath('data.message', '가격 정보가 삭제되었습니다.');

        $deleteSeller = $this->actingAsApiUser($admin)->deleteJson('/api/v1/sellers/'.$sellerId);
        $deleteSeller->assertOk();
        $deleteSeller->assertJsonPath('data.message', '판매처가 삭제되었습니다.');
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('ssp-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'SSP User',
            'nickname' => 'ssp-user',
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
