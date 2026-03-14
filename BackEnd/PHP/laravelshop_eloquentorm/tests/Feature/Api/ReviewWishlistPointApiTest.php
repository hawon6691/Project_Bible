<?php

namespace Tests\Feature\Api;

use App\Models\Address;
use App\Models\Category;
use App\Models\Order;
use App\Models\OrderItem;
use App\Models\Product;
use App\Models\Seller;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class ReviewWishlistPointApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_create_update_delete_review_and_earn_points(): void
    {
        $user = $this->createUser('USER');
        $category = Category::query()->create([
            'name' => '노트북',
            'slug' => 'laptop',
            'depth' => 0,
            'sort_order' => 0,
            'is_visible' => true,
        ]);
        $seller = Seller::query()->create([
            'name' => 'PB Store',
            'code' => 'pb-store',
            'status' => 'ACTIVE',
            'rating' => 4.8,
        ]);
        $product = Product::query()->create([
            'category_id' => $category->id,
            'name' => 'PB Laptop',
            'slug' => 'pb-laptop',
            'status' => 'ACTIVE',
        ]);
        $address = Address::query()->create([
            'user_id' => $user->id,
            'recipient_name' => '홍길동',
            'phone' => '01012341234',
            'zip_code' => '06236',
            'address_line1' => '서울시 강남구',
        ]);
        $order = Order::query()->create([
            'user_id' => $user->id,
            'address_id' => $address->id,
            'order_number' => 'ORD-TEST-001',
            'status' => 'ORDER_PLACED',
            'total_amount' => 1500000,
            'final_amount' => 1500000,
            'recipient_name' => '홍길동',
            'phone' => '01012341234',
            'zip_code' => '06236',
            'address_line1' => '서울시 강남구',
        ]);
        OrderItem::query()->create([
            'order_id' => $order->id,
            'product_id' => $product->id,
            'seller_id' => $seller->id,
            'product_name' => $product->name,
            'quantity' => 1,
            'unit_price' => 1500000,
            'shipping_fee' => 0,
        ]);

        $createReview = $this->actingAsApiUser($user)->postJson('/api/v1/products/'.$product->id.'/reviews', [
            'orderId' => $order->id,
            'rating' => 5,
            'content' => '정말 만족스럽습니다.',
        ]);
        $createReview->assertCreated();
        $reviewId = $createReview->json('data.id');
        $createReview->assertJsonPath('data.rating', 5);

        $listReviews = $this->getJson('/api/v1/products/'.$product->id.'/reviews');
        $listReviews->assertOk();
        $listReviews->assertJsonPath('data.0.content', '정말 만족스럽습니다.');

        $updateReview = $this->actingAsApiUser($user)->patchJson('/api/v1/reviews/'.$reviewId, [
            'rating' => 4,
            'content' => '수정된 리뷰입니다.',
        ]);
        $updateReview->assertOk();
        $updateReview->assertJsonPath('data.rating', 4);

        $balanceResponse = $this->actingAsApiUser($user)->getJson('/api/v1/points/balance');
        $balanceResponse->assertOk();
        $this->assertEquals(500.0, $balanceResponse->json('data.balance'));

        $transactionsResponse = $this->actingAsApiUser($user)->getJson('/api/v1/points/transactions');
        $transactionsResponse->assertOk();
        $transactionsResponse->assertJsonPath('data.items.0.type', 'EARN');

        $deleteReview = $this->actingAsApiUser($user)->deleteJson('/api/v1/reviews/'.$reviewId);
        $deleteReview->assertOk();
        $deleteReview->assertJsonPath('data.message', '리뷰가 삭제되었습니다.');
    }

    public function test_user_can_toggle_wishlist_and_admin_can_grant_points(): void
    {
        $user = $this->createUser('USER');
        $admin = $this->createUser('ADMIN');
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

        $toggleOn = $this->actingAsApiUser($user)->postJson('/api/v1/wishlist/'.$product->id);
        $toggleOn->assertOk();
        $toggleOn->assertJsonPath('data.wishlisted', true);

        $listWishlist = $this->actingAsApiUser($user)->getJson('/api/v1/wishlist');
        $listWishlist->assertOk();
        $listWishlist->assertJsonPath('data.items.0.productName', 'PB Tablet');

        $toggleOff = $this->actingAsApiUser($user)->postJson('/api/v1/wishlist/'.$product->id);
        $toggleOff->assertOk();
        $toggleOff->assertJsonPath('data.wishlisted', false);

        $grantPoints = $this->actingAsApiUser($admin)->postJson('/api/v1/admin/points/grant', [
            'userId' => $user->id,
            'amount' => 3000,
            'description' => '관리자 수동 지급',
        ]);
        $grantPoints->assertCreated();
        $grantPoints->assertJsonPath('data.type', 'ADMIN_GRANT');

        $transactionsResponse = $this->actingAsApiUser($user)->getJson('/api/v1/points/transactions?type=ADMIN_GRANT');
        $transactionsResponse->assertOk();
        $this->assertEquals(3000.0, $transactionsResponse->json('data.items.0.amount'));

        $deleteWishlist = $this->actingAsApiUser($user)->deleteJson('/api/v1/wishlist/'.$product->id);
        $deleteWishlist->assertOk();
        $deleteWishlist->assertJsonPath('data.message', '위시리스트에서 제거되었습니다.');
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('rwp-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Review Wishlist Point User',
            'nickname' => 'rwp-user',
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
