<?php

namespace Tests\Feature\Api;

use App\Models\Category;
use App\Models\Friendship;
use App\Models\Product;
use App\Models\ProductMapping;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class PcFriendShortformMediaNewsMatchingApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_pc_builder_and_friend_flow(): void
    {
        $admin = $this->createUser('ADMIN');
        $user = $this->createUser('USER');
        $friend = $this->createUser('USER');
        $category = $this->createCategory();
        $product = $this->createProduct($category->id, 'PB CPU', 'pb-cpu');

        $createBuild = $this->actingAsApiUser($user)->postJson('/api/v1/pc-builds', [
            'name' => '내 게이밍 PC',
            'description' => '테스트 견적',
        ]);
        $createBuild->assertCreated();
        $buildId = $createBuild->json('data.id');

        $addPart = $this->actingAsApiUser($user)->postJson('/api/v1/pc-builds/'.$buildId.'/parts', [
            'partType' => 'CPU',
            'productId' => $product->id,
            'quantity' => 1,
        ]);
        $addPart->assertCreated();
        $addPart->assertJsonPath('data.parts.0.product.name', 'PB CPU');

        $share = $this->actingAsApiUser($user)->getJson('/api/v1/pc-builds/'.$buildId.'/share');
        $share->assertOk();
        $shareCode = $share->json('data.shareCode');

        $shared = $this->getJson('/api/v1/pc-builds/shared/'.$shareCode);
        $shared->assertOk();
        $shared->assertJsonPath('data.name', '내 게이밍 PC');

        $rule = $this->actingAsApiUser($admin)->postJson('/api/v1/admin/compatibility-rules', [
            'name' => '기본 CPU 규칙',
            'sourcePartType' => 'CPU',
            'targetPartType' => 'MAINBOARD',
            'ruleType' => 'CATEGORY_MISMATCH',
            'ruleValue' => ['key' => 'value'],
        ]);
        $rule->assertCreated();

        $friendRequest = $this->actingAsApiUser($user)->postJson('/api/v1/friends/request/'.$friend->id);
        $friendRequest->assertCreated();

        $friendshipId = Friendship::query()->value('id');
        $accept = $this->actingAsApiUser($friend)->patchJson('/api/v1/friends/request/'.$friendshipId.'/accept');
        $accept->assertOk();

        $friends = $this->actingAsApiUser($user)->getJson('/api/v1/friends');
        $friends->assertOk();
        $friends->assertJsonPath('data.items.0.friend.id', $friend->id);
    }

    public function test_shortform_media_news_and_matching_flow(): void
    {
        $admin = $this->createUser('ADMIN');
        $user = $this->createUser('USER');
        $category = $this->createCategory();
        $product = $this->createProduct($category->id, 'PB Camera', 'pb-camera');

        $shortform = $this->actingAsApiUser($user)->postJson('/api/v1/shortforms', [
            'title' => '언박싱 숏폼',
            'videoUrl' => 'https://example.com/video.mp4',
            'thumbnailUrl' => 'https://example.com/thumb.jpg',
            'productIds' => [$product->id],
        ]);
        $shortform->assertCreated();
        $shortformId = $shortform->json('data.id');

        $like = $this->actingAsApiUser($admin)->postJson('/api/v1/shortforms/'.$shortformId.'/like');
        $like->assertOk();
        $like->assertJsonPath('data.liked', true);

        $comment = $this->actingAsApiUser($admin)->postJson('/api/v1/shortforms/'.$shortformId.'/comments', [
            'content' => '좋은 영상입니다.',
        ]);
        $comment->assertCreated();

        $upload = $this->actingAsApiUser($user)->postJson('/api/v1/media/upload', [
            'files' => [[
                'fileName' => 'manual.pdf',
                'fileUrl' => 'https://example.com/manual.pdf',
                'mimeType' => 'application/pdf',
                'size' => 1024,
            ]],
            'ownerType' => 'PRODUCT',
            'ownerId' => $product->id,
        ]);
        $upload->assertCreated();
        $mediaId = $upload->json('data.0.id');

        $metadata = $this->getJson('/api/v1/media/'.$mediaId.'/metadata');
        $metadata->assertOk();
        $metadata->assertJsonPath('data.mime', 'application/pdf');

        $categoryResponse = $this->actingAsApiUser($admin)->postJson('/api/v1/news/categories', [
            'name' => '리뷰',
            'slug' => 'review',
        ]);
        $categoryResponse->assertCreated();
        $newsCategoryId = $categoryResponse->json('data.id');

        $news = $this->actingAsApiUser($admin)->postJson('/api/v1/news', [
            'title' => 'PB Camera 출시',
            'content' => '신제품 소식',
            'categoryId' => $newsCategoryId,
            'thumbnailUrl' => 'https://example.com/news.jpg',
            'productIds' => [$product->id],
        ]);
        $news->assertCreated();
        $newsId = $news->json('data.id');

        $newsDetail = $this->getJson('/api/v1/news/'.$newsId);
        $newsDetail->assertOk();
        $newsDetail->assertJsonPath('data.products.0.name', 'PB Camera');

        ProductMapping::query()->create([
            'source_name' => 'PB Camera',
            'status' => 'PENDING',
        ]);

        $pending = $this->actingAsApiUser($admin)->getJson('/api/v1/matching/pending');
        $pending->assertOk();
        $mappingId = $pending->json('data.items.0.id');

        $approve = $this->actingAsApiUser($admin)->patchJson('/api/v1/matching/'.$mappingId.'/approve', [
            'productId' => $product->id,
        ]);
        $approve->assertOk();

        $stats = $this->actingAsApiUser($admin)->getJson('/api/v1/matching/stats');
        $stats->assertOk();
        $stats->assertJsonPath('data.approved', 1);
    }

    private function createCategory(): Category
    {
        return Category::query()->create([
            'name' => '부품',
            'slug' => uniqid('parts-', true),
            'depth' => 0,
            'sort_order' => 0,
            'is_visible' => true,
        ]);
    }

    private function createProduct(int $categoryId, string $name, string $slug): Product
    {
        return Product::query()->create([
            'category_id' => $categoryId,
            'name' => $name,
            'slug' => $slug.'-'.uniqid(),
            'status' => 'ACTIVE',
        ]);
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('pfsnm-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'PB User',
            'nickname' => uniqid('pb-', false),
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
