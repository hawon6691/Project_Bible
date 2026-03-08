<?php

namespace Tests\Feature\Api;

use App\Models\Address;
use App\Models\CartItem;
use App\Models\Category;
use App\Models\Product;
use App\Models\Seller;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class OrderPaymentApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_create_list_view_cancel_order_and_pay(): void
    {
        $user = $this->createUser('USER');
        $admin = $this->createUser('ADMIN');
        $address = Address::query()->create([
            'user_id' => $user->id,
            'recipient_name' => '홍길동',
            'phone' => '01012341234',
            'zip_code' => '06236',
            'address_line1' => '서울시 강남구 테헤란로 1',
            'is_default' => true,
        ]);
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
            'rating' => 4.8,
        ]);
        \App\Models\PriceEntry::query()->create([
            'product_id' => $product->id,
            'seller_id' => $seller->id,
            'price' => 1500000,
            'shipping_fee' => 0,
            'stock_status' => 'IN_STOCK',
            'collected_at' => now(),
        ]);
        $cartItem = CartItem::query()->create([
            'user_id' => $user->id,
            'product_id' => $product->id,
            'seller_id' => $seller->id,
            'quantity' => 2,
            'selected_options' => '16GB / 512GB',
        ]);

        $createOrder = $this->actingAsApiUser($user)->postJson('/api/v1/orders', [
            'addressId' => $address->id,
            'fromCart' => true,
            'cartItemIds' => [$cartItem->id],
            'usePoint' => 5000,
            'memo' => '문 앞에 놓아주세요',
        ]);
        $createOrder->assertCreated();
        $orderId = $createOrder->json('data.id');
        $createOrder->assertJsonPath('data.status', 'ORDER_PLACED');

        $listOrders = $this->actingAsApiUser($user)->getJson('/api/v1/orders');
        $listOrders->assertOk();
        $listOrders->assertJsonPath('data.items.0.id', $orderId);

        $showOrder = $this->actingAsApiUser($user)->getJson('/api/v1/orders/' . $orderId);
        $showOrder->assertOk();
        $showOrder->assertJsonPath('data.items.0.productName', 'PB Laptop');

        $adminOrders = $this->actingAsApiUser($admin)->getJson('/api/v1/admin/orders');
        $adminOrders->assertOk();
        $adminOrders->assertJsonPath('data.items.0.id', $orderId);

        $adminStatusUpdate = $this->actingAsApiUser($admin)->patchJson('/api/v1/admin/orders/' . $orderId . '/status', [
            'status' => 'SHIPPED',
        ]);
        $adminStatusUpdate->assertOk();
        $adminStatusUpdate->assertJsonPath('data.status', 'SHIPPED');

        $createPayment = $this->actingAsApiUser($user)->postJson('/api/v1/payments', [
            'orderId' => $orderId,
            'method' => 'CARD',
            'provider' => 'MOCK_PG',
        ]);
        $createPayment->assertCreated();
        $paymentId = $createPayment->json('data.id');
        $createPayment->assertJsonPath('data.status', 'PAID');

        $showPayment = $this->actingAsApiUser($user)->getJson('/api/v1/payments/' . $paymentId);
        $showPayment->assertOk();
        $showPayment->assertJsonPath('data.orderId', $orderId);

        $refundPayment = $this->actingAsApiUser($user)->postJson('/api/v1/payments/' . $paymentId . '/refund');
        $refundPayment->assertOk();
        $refundPayment->assertJsonPath('data.status', 'REFUNDED');

        $cancelOrder = $this->actingAsApiUser($user)->postJson('/api/v1/orders/' . $orderId . '/cancel');
        $cancelOrder->assertOk();
        $cancelOrder->assertJsonPath('data.status', 'CANCELLED');
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('order-payment-', true) . '@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Order Payment User',
            'nickname' => 'order-payment-user',
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
