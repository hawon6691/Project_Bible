<?php

namespace Tests\Feature\Api;

use App\Models\Category;
use App\Models\Product;
use App\Models\Seller;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class CartAddressApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_manage_addresses(): void
    {
        $user = $this->createUser('USER');

        $createResponse = $this->actingAsApiUser($user)->postJson('/api/v1/addresses', [
            'recipientName' => '홍길동',
            'label' => '집',
            'phone' => '01012341234',
            'zipCode' => '06236',
            'addressLine1' => '서울시 강남구 테헤란로 1',
            'addressLine2' => '101호',
            'memo' => '문 앞에 놓아주세요',
            'isDefault' => true,
        ]);
        $createResponse->assertCreated();
        $addressId = $createResponse->json('data.id');
        $createResponse->assertJsonPath('data.isDefault', true);

        $listResponse = $this->actingAsApiUser($user)->getJson('/api/v1/addresses');
        $listResponse->assertOk();
        $listResponse->assertJsonPath('data.0.label', '집');

        $updateResponse = $this->actingAsApiUser($user)->patchJson('/api/v1/addresses/'.$addressId, [
            'label' => '회사',
            'isDefault' => true,
        ]);
        $updateResponse->assertOk();
        $updateResponse->assertJsonPath('data.label', '회사');

        $deleteResponse = $this->actingAsApiUser($user)->deleteJson('/api/v1/addresses/'.$addressId);
        $deleteResponse->assertOk();
        $deleteResponse->assertJsonPath('data.message', '배송지가 삭제되었습니다.');
    }

    public function test_user_can_manage_cart_items(): void
    {
        $user = $this->createUser('USER');
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
        $seller = Seller::query()->create([
            'name' => 'PB Store',
            'code' => 'pb-store',
            'status' => 'ACTIVE',
            'rating' => 4.9,
        ]);

        $addResponse = $this->actingAsApiUser($user)->postJson('/api/v1/cart', [
            'productId' => $product->id,
            'sellerId' => $seller->id,
            'quantity' => 2,
            'selectedOptions' => '16GB / 512GB',
        ]);
        $addResponse->assertCreated();
        $itemId = $addResponse->json('data.id');
        $addResponse->assertJsonPath('data.quantity', 2);

        $mergeResponse = $this->actingAsApiUser($user)->postJson('/api/v1/cart', [
            'productId' => $product->id,
            'sellerId' => $seller->id,
            'quantity' => 1,
            'selectedOptions' => '16GB / 512GB',
        ]);
        $mergeResponse->assertCreated();
        $mergeResponse->assertJsonPath('data.quantity', 3);

        $listResponse = $this->actingAsApiUser($user)->getJson('/api/v1/cart');
        $listResponse->assertOk();
        $listResponse->assertJsonPath('data.0.productName', 'PB Laptop');

        $updateResponse = $this->actingAsApiUser($user)->patchJson('/api/v1/cart/'.$itemId, [
            'quantity' => 5,
        ]);
        $updateResponse->assertOk();
        $updateResponse->assertJsonPath('data.quantity', 5);

        $deleteResponse = $this->actingAsApiUser($user)->deleteJson('/api/v1/cart/'.$itemId);
        $deleteResponse->assertOk();
        $deleteResponse->assertJsonPath('data.message', '장바구니 항목이 삭제되었습니다.');

        $this->actingAsApiUser($user)->postJson('/api/v1/cart', [
            'productId' => $product->id,
            'sellerId' => $seller->id,
            'quantity' => 1,
        ])->assertCreated();

        $clearResponse = $this->actingAsApiUser($user)->deleteJson('/api/v1/cart');
        $clearResponse->assertOk();
        $clearResponse->assertJsonPath('data.message', '장바구니가 비워졌습니다.');
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('cart-address-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Cart Address User',
            'nickname' => 'cart-address-user',
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
