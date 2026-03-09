<?php

namespace Tests\Feature\Api;

use App\Models\Category;
use App\Models\Product;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class ActivityChatPushApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_manage_activity_history(): void
    {
        $user = $this->createUser('USER');
        $category = Category::query()->create([
            'name' => '노트북',
            'slug' => 'laptops',
            'depth' => 0,
            'sort_order' => 0,
            'is_visible' => true,
        ]);
        $product = Product::query()->create([
            'category_id' => $category->id,
            'name' => 'PB Laptop Air',
            'slug' => 'pb-laptop-air',
            'status' => 'ACTIVE',
        ]);

        $addView = $this->actingAsApiUser($user)->postJson('/api/v1/activities/recent-products/' . $product->id);
        $addView->assertCreated();
        $addView->assertJsonPath('data.productName', 'PB Laptop Air');

        $addSearch = $this->actingAsApiUser($user)->postJson('/api/v1/activities/searches', [
            'keyword' => '게이밍 노트북',
        ]);
        $addSearch->assertCreated();
        $searchId = $addSearch->json('data.id');

        $summary = $this->actingAsApiUser($user)->getJson('/api/v1/activities');
        $summary->assertOk();
        $summary->assertJsonPath('data.recentProductCount', 1);
        $summary->assertJsonPath('data.searchCount', 1);

        $recentProducts = $this->actingAsApiUser($user)->getJson('/api/v1/activities/recent-products?page=1&limit=10');
        $recentProducts->assertOk();
        $recentProducts->assertJsonPath('data.items.0.productName', 'PB Laptop Air');

        $searches = $this->actingAsApiUser($user)->getJson('/api/v1/activities/searches');
        $searches->assertOk();
        $searches->assertJsonPath('data.items.0.keyword', '게이밍 노트북');

        $deleteSearch = $this->actingAsApiUser($user)->deleteJson('/api/v1/activities/searches/' . $searchId);
        $deleteSearch->assertOk();
        $deleteSearch->assertJsonPath('data.message', '검색 기록이 삭제되었습니다.');

        $clearSearches = $this->actingAsApiUser($user)->deleteJson('/api/v1/activities/searches');
        $clearSearches->assertOk();
        $clearSearches->assertJsonPath('data.message', '검색 기록이 전체 삭제되었습니다.');
    }

    public function test_users_can_create_join_and_send_chat_messages(): void
    {
        $owner = $this->createUser('USER');
        $guest = $this->createUser('USER');

        $createRoom = $this->actingAsApiUser($owner)->postJson('/api/v1/chat/rooms', [
            'name' => '배송 문의 채팅',
            'isPrivate' => true,
        ]);
        $createRoom->assertCreated();
        $roomId = $createRoom->json('data.id');
        $createRoom->assertJsonPath('data.members.0.userId', $owner->id);

        $joinRoom = $this->actingAsApiUser($guest)->postJson('/api/v1/chat/rooms/' . $roomId . '/join');
        $joinRoom->assertOk();

        $sendMessage = $this->actingAsApiUser($owner)->postJson('/api/v1/chat/rooms/' . $roomId . '/messages', [
            'message' => '안녕하세요. 문의드립니다.',
        ]);
        $sendMessage->assertCreated();
        $sendMessage->assertJsonPath('data.message', '안녕하세요. 문의드립니다.');

        $roomList = $this->actingAsApiUser($owner)->getJson('/api/v1/chat/rooms');
        $roomList->assertOk();
        $roomList->assertJsonPath('data.items.0.name', '배송 문의 채팅');

        $messages = $this->actingAsApiUser($guest)->getJson('/api/v1/chat/rooms/' . $roomId . '/messages');
        $messages->assertOk();
        $messages->assertJsonPath('data.items.0.message', '안녕하세요. 문의드립니다.');
    }

    public function test_user_can_manage_push_subscriptions_and_preferences(): void
    {
        $user = $this->createUser('USER');

        $register = $this->actingAsApiUser($user)->postJson('/api/v1/push/subscriptions', [
            'endpoint' => 'https://push.example.com/sub/1',
            'p256dhKey' => 'p256dh-key',
            'authKey' => 'auth-key',
            'expirationTime' => '1741000000000',
        ]);
        $register->assertCreated();
        $register->assertJsonPath('data.isActive', true);

        $subscriptions = $this->actingAsApiUser($user)->getJson('/api/v1/push/subscriptions');
        $subscriptions->assertOk();
        $subscriptions->assertJsonPath('data.0.endpoint', 'https://push.example.com/sub/1');

        $preference = $this->actingAsApiUser($user)->getJson('/api/v1/push/preferences');
        $preference->assertOk();
        $preference->assertJsonPath('data.priceAlertEnabled', true);

        $updatePreference = $this->actingAsApiUser($user)->postJson('/api/v1/push/preferences', [
            'priceAlertEnabled' => false,
            'chatMessageEnabled' => false,
        ]);
        $updatePreference->assertOk();
        $updatePreference->assertJsonPath('data.priceAlertEnabled', false);
        $updatePreference->assertJsonPath('data.chatMessageEnabled', false);

        $unsubscribe = $this->actingAsApiUser($user)->postJson('/api/v1/push/subscriptions/unsubscribe', [
            'endpoint' => 'https://push.example.com/sub/1',
        ]);
        $unsubscribe->assertOk();
        $unsubscribe->assertJsonPath('data.message', '푸시 구독이 해제되었습니다.');
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('acp-', true) . '@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Activity Chat Push User',
            'nickname' => 'acp-user',
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
