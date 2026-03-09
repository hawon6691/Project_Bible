<?php

namespace Tests\Feature\Api;

use App\Models\Badge;
use App\Models\Category;
use App\Models\ExchangeRate;
use App\Models\PriceEntry;
use App\Models\Product;
use App\Models\Seller;
use App\Models\Translation;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

class FraudTrustI18nImageBadgeApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_fraud_and_trust_flow(): void
    {
        $admin = $this->createUser('ADMIN');
        $category = $this->createCategory();
        $seller = Seller::query()->create([
            'name' => 'PB Seller',
            'code' => 'pb-seller',
            'status' => 'ACTIVE',
            'rating' => 4.6,
        ]);
        $product = Product::query()->create([
            'category_id' => $category->id,
            'name' => 'PB Tablet',
            'slug' => 'pb-tablet-' . uniqid(),
            'status' => 'ACTIVE',
        ]);

        PriceEntry::query()->create([
            'product_id' => $product->id,
            'seller_id' => $seller->id,
            'price' => 1000000,
            'shipping_fee' => 0,
            'stock_status' => 'IN_STOCK',
            'collected_at' => now()->subDay(),
        ]);
        $cheapEntry = PriceEntry::query()->create([
            'product_id' => $product->id,
            'seller_id' => $seller->id,
            'price' => 500000,
            'shipping_fee' => 3000,
            'stock_status' => 'IN_STOCK',
            'collected_at' => now(),
        ]);

        $realPrice = $this->getJson('/api/v1/products/' . $product->id . '/real-price?sellerId=' . $seller->id);
        $realPrice->assertOk();
        $realPrice->assertJsonPath('data.totalPrice', 503000);

        $scan = $this->actingAsApiUser($admin)->postJson('/api/v1/fraud/admin/products/' . $product->id . '/scan');
        $scan->assertOk();
        $scan->assertJsonPath('data.items.0.priceEntryId', $cheapEntry->id);

        $flags = $this->actingAsApiUser($admin)->getJson('/api/v1/fraud/admin/products/' . $product->id . '/flags');
        $flags->assertOk();
        $flagId = $flags->json('data.0.id');

        $alerts = $this->actingAsApiUser($admin)->getJson('/api/v1/fraud/alerts');
        $alerts->assertOk();

        $approve = $this->actingAsApiUser($admin)->patchJson('/api/v1/fraud/alerts/' . $flagId . '/approve');
        $approve->assertOk();
        $approve->assertJsonPath('data.message', '이상 가격 알림이 승인되었습니다.');

        $trust = $this->getJson('/api/v1/trust/sellers/' . $seller->id);
        $trust->assertOk();
        $trust->assertJsonPath('data.sellerName', 'PB Seller');

        $history = $this->getJson('/api/v1/trust/sellers/' . $seller->id . '/history');
        $history->assertOk();

        $recalculate = $this->actingAsApiUser($admin)->postJson('/api/v1/trust/admin/sellers/' . $seller->id . '/recalculate');
        $recalculate->assertOk();
    }

    public function test_i18n_and_image_flow(): void
    {
        Storage::fake('public');
        $admin = $this->createUser('ADMIN');

        $translation = $this->actingAsApiUser($admin)->postJson('/api/v1/i18n/admin/translations', [
            'locale' => 'en',
            'namespace' => 'product',
            'key' => 'product.lowest_price',
            'value' => 'Lowest Price',
        ]);
        $translation->assertOk();
        $translationId = $translation->json('data.id');

        $translations = $this->getJson('/api/v1/i18n/translations?locale=en&namespace=product');
        $translations->assertOk();
        $translations->assertJsonPath('data.0.value', 'Lowest Price');

        $rate = $this->actingAsApiUser($admin)->postJson('/api/v1/i18n/admin/exchange-rates', [
            'baseCurrency' => 'KRW',
            'targetCurrency' => 'USD',
            'rate' => 0.000748,
        ]);
        $rate->assertOk();

        $exchangeRates = $this->getJson('/api/v1/i18n/exchange-rates');
        $exchangeRates->assertOk();
        $exchangeRates->assertJsonPath('data.0.baseCurrency', 'KRW');

        $convert = $this->getJson('/api/v1/i18n/convert?amount=1590000&from=KRW&to=USD');
        $convert->assertOk();
        $convert->assertJsonPath('data.rate', 0.000748);

        $upload = $this->actingAsApiUser($admin)->postJson('/api/v1/images/upload', [
            'file' => UploadedFile::fake()->create('badge.jpg', 100, 'image/jpeg'),
            'category' => 'product',
        ]);
        $upload->assertCreated();
        $imageId = $upload->json('data.id');

        $variants = $this->getJson('/api/v1/images/' . $imageId . '/variants');
        $variants->assertOk();
        $variants->assertJsonPath('data.0.type', 'THUMBNAIL');

        $deleteTranslation = $this->actingAsApiUser($admin)->deleteJson('/api/v1/i18n/admin/translations/' . $translationId);
        $deleteTranslation->assertOk();

        $deleteImage = $this->actingAsApiUser($admin)->deleteJson('/api/v1/images/' . $imageId);
        $deleteImage->assertOk();
    }

    public function test_badge_flow(): void
    {
        $admin = $this->createUser('ADMIN');
        $user = $this->createUser('USER');

        $createBadge = $this->actingAsApiUser($admin)->postJson('/api/v1/admin/badges', [
            'name' => '리뷰 마스터',
            'description' => '리뷰 10개 이상 작성',
            'iconUrl' => '/badges/review-master.svg',
            'type' => 'AUTO',
            'condition' => ['metric' => 'review_count', 'threshold' => 10],
            'rarity' => 'COMMON',
        ]);
        $createBadge->assertCreated();
        $badgeId = $createBadge->json('data.id');

        $badges = $this->getJson('/api/v1/badges');
        $badges->assertOk();
        $badges->assertJsonPath('data.0.name', '리뷰 마스터');

        $grant = $this->actingAsApiUser($admin)->postJson('/api/v1/admin/badges/' . $badgeId . '/grant', [
            'userId' => $user->id,
        ]);
        $grant->assertCreated();

        $me = $this->actingAsApiUser($user)->getJson('/api/v1/badges/me');
        $me->assertOk();
        $me->assertJsonPath('data.0.badge.name', '리뷰 마스터');

        $userBadges = $this->getJson('/api/v1/users/' . $user->id . '/badges');
        $userBadges->assertOk();

        $updateBadge = $this->actingAsApiUser($admin)->patchJson('/api/v1/admin/badges/' . $badgeId, [
            'rarity' => 'RARE',
        ]);
        $updateBadge->assertOk();
        $updateBadge->assertJsonPath('data.rarity', 'RARE');

        $revoke = $this->actingAsApiUser($admin)->deleteJson('/api/v1/admin/badges/' . $badgeId . '/revoke/' . $user->id);
        $revoke->assertOk();

        $deleteBadge = $this->actingAsApiUser($admin)->deleteJson('/api/v1/admin/badges/' . $badgeId);
        $deleteBadge->assertOk();
    }

    private function createCategory()
    {
        return Category::query()->create([
            'name' => '기본',
            'slug' => 'base-' . uniqid(),
            'depth' => 0,
            'sort_order' => 0,
            'is_visible' => true,
        ]);
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('ftiib-', true) . '@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Fraud Trust I18n Image Badge User',
            'nickname' => 'ftiib-user',
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
