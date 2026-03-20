<?php

namespace Tests\Feature\Api;

use App\Models\Category;
use App\Models\PriceEntry;
use App\Models\Product;
use App\Models\Recommendation;
use App\Models\Seller;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class PredictionDealRecommendationRankingApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_prediction_and_deal_flow(): void
    {
        $admin = $this->createUser('ADMIN');
        $category = $this->createCategory();
        $product = $this->createProduct($category->id, 'PB UltraBook', 'pb-ultrabook');
        $seller = Seller::query()->create([
            'name' => 'PB Store',
            'code' => 'pb-store',
            'status' => 'ACTIVE',
            'rating' => 4.8,
        ]);

        PriceEntry::query()->create([
            'product_id' => $product->id,
            'seller_id' => $seller->id,
            'price' => 1500000,
            'shipping_fee' => 0,
            'stock_status' => 'IN_STOCK',
            'collected_at' => now()->subDays(3),
        ]);
        PriceEntry::query()->create([
            'product_id' => $product->id,
            'seller_id' => $seller->id,
            'price' => 1450000,
            'shipping_fee' => 0,
            'stock_status' => 'IN_STOCK',
            'collected_at' => now()->subDay(),
        ]);

        $prediction = $this->getJson('/api/v1/predictions/products/'.$product->id.'/price-trend?days=7');
        $prediction->assertOk();
        $prediction->assertJsonPath('data.productName', 'PB UltraBook');

        $createDeal = $this->actingAsApiUser($admin)->postJson('/api/v1/deals/admin', [
            'productId' => $product->id,
            'title' => '봄맞이 특가',
            'type' => 'SPECIAL',
            'description' => '한정 수량 할인',
            'dealPrice' => 1390000,
            'discountRate' => 7.5,
            'stock' => 20,
            'startAt' => now()->toISOString(),
            'endAt' => now()->addDays(7)->toISOString(),
        ]);
        $createDeal->assertCreated();
        $dealId = $createDeal->json('data.id');

        $listDeals = $this->getJson('/api/v1/deals');
        $listDeals->assertOk();
        $listDeals->assertJsonPath('data.0.title', '봄맞이 특가');

        $updateDeal = $this->actingAsApiUser($admin)->patchJson('/api/v1/deals/admin/'.$dealId, [
            'stock' => 15,
        ]);
        $updateDeal->assertOk();
        $updateDeal->assertJsonPath('data.stock', 15);

        $deleteDeal = $this->actingAsApiUser($admin)->deleteJson('/api/v1/deals/admin/'.$dealId);
        $deleteDeal->assertOk();
        $deleteDeal->assertJsonPath('data.message', '특가가 삭제되었습니다.');
    }

    public function test_recommendation_and_ranking_flow(): void
    {
        $admin = $this->createUser('ADMIN');
        $user = $this->createUser('USER');
        $category = $this->createCategory();
        $productA = $this->createProduct($category->id, 'PB Monitor', 'pb-monitor');
        $productB = $this->createProduct($category->id, 'PB Keyboard', 'pb-keyboard');

        Recommendation::query()->create([
            'product_id' => $productA->id,
            'type' => 'TRENDING',
            'title' => '오늘의 추천',
            'reason' => '최근 반응이 좋은 상품입니다.',
            'score' => 100,
            'is_active' => true,
        ]);

        $trending = $this->getJson('/api/v1/recommendations/trending?limit=10');
        $trending->assertOk();
        $trending->assertJsonPath('data.0.product.name', 'PB Monitor');

        $createRecommendation = $this->actingAsApiUser($admin)->postJson('/api/v1/admin/recommendations', [
            'productId' => $productB->id,
            'type' => 'PERSONAL',
            'title' => '맞춤 추천',
            'reason' => '액세서리 구매 패턴 기반 추천',
            'score' => 80,
            'isActive' => true,
        ]);
        $createRecommendation->assertCreated();
        $recommendationId = $createRecommendation->json('data.id');

        $personal = $this->actingAsApiUser($user)->getJson('/api/v1/recommendations/personal?limit=10');
        $personal->assertOk();

        $adminList = $this->actingAsApiUser($admin)->getJson('/api/v1/admin/recommendations');
        $adminList->assertOk();

        $this->actingAsApiUser($user)->postJson('/api/v1/activities/recent-products/'.$productA->id)->assertCreated();
        $this->actingAsApiUser($user)->postJson('/api/v1/activities/searches', ['keyword' => '모니터'])->assertCreated();
        $this->actingAsApiUser($user)->postJson('/api/v1/activities/searches', ['keyword' => '모니터'])->assertCreated();

        $popularProducts = $this->getJson('/api/v1/rankings/products/popular?limit=10');
        $popularProducts->assertOk();
        $popularProducts->assertJsonPath('data.0.product.name', 'PB Monitor');

        $popularKeywords = $this->getJson('/api/v1/rankings/keywords/popular?limit=10');
        $popularKeywords->assertOk();
        $popularKeywords->assertJsonPath('data.0.keyword', '모니터');

        $recalculate = $this->actingAsApiUser($admin)->postJson('/api/v1/rankings/admin/recalculate');
        $recalculate->assertOk();
        $recalculate->assertJsonStructure(['data' => ['updatedCount']]);

        $deleteRecommendation = $this->actingAsApiUser($admin)->deleteJson('/api/v1/admin/recommendations/'.$recommendationId);
        $deleteRecommendation->assertOk();
        $deleteRecommendation->assertJsonPath('data.message', '추천이 삭제되었습니다.');
    }

    private function createCategory()
    {
        return Category::query()->create([
            'name' => '기기',
            'slug' => uniqid('devices-', true),
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
            'review_count' => 5,
        ]);
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('pdrr-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Prediction Deal Recommendation Ranking User',
            'nickname' => 'pdrr-user',
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
