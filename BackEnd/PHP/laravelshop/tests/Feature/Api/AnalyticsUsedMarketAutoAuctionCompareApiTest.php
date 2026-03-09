<?php

namespace Tests\Feature\Api;

use App\Models\AutoLeaseOffer;
use App\Models\AutoModel;
use App\Models\AutoOption;
use App\Models\AutoTrim;
use App\Models\Category;
use App\Models\PcBuild;
use App\Models\PriceEntry;
use App\Models\Product;
use App\Models\Seller;
use App\Models\UsedMarketPrice;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class AnalyticsUsedMarketAutoAuctionCompareApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_analytics_used_market_and_auto_flow(): void
    {
        $user = $this->createUser('USER');
        $category = $this->createCategory();
        $product = $this->createProduct($category->id, 'PB SSD', 'pb-ssd');
        $seller = $this->createSeller();

        PriceEntry::query()->create(['product_id' => $product->id, 'seller_id' => $seller->id, 'price' => 150000, 'shipping_fee' => 0, 'stock_status' => 'IN_STOCK', 'collected_at' => now()->subDays(2)]);
        PriceEntry::query()->create(['product_id' => $product->id, 'seller_id' => $seller->id, 'price' => 140000, 'shipping_fee' => 0, 'stock_status' => 'IN_STOCK', 'collected_at' => now()]);
        UsedMarketPrice::query()->create(['product_id' => $product->id, 'price' => 90000]);
        UsedMarketPrice::query()->create(['product_id' => $product->id, 'price' => 95000]);

        $build = PcBuild::query()->create(['user_id' => $user->id, 'name' => '중고 견적']);
        \App\Models\PcBuildPart::query()->create(['pc_build_id' => $build->id, 'part_type' => 'SSD', 'product_id' => $product->id, 'quantity' => 1]);

        $model = AutoModel::query()->create(['brand' => 'PB Motors', 'name' => 'PB E-Car', 'type' => 'EV']);
        $trim = AutoTrim::query()->create(['auto_model_id' => $model->id, 'name' => 'Long Range', 'base_price' => 52000000]);
        $option = AutoOption::query()->create(['auto_trim_id' => $trim->id, 'name' => 'HUD', 'price' => 1200000]);
        AutoLeaseOffer::query()->create(['auto_model_id' => $model->id, 'provider' => 'PB Lease', 'monthly_payment' => 590000, 'contract_months' => 48]);

        $this->getJson('/api/v1/analytics/products/'.$product->id.'/lowest-ever')->assertOk()->assertJsonPath('data.lowestPrice', 140000);
        $this->getJson('/api/v1/used-market/products/'.$product->id.'/price')->assertOk()->assertJsonPath('data.averagePrice', 92500);
        $this->actingAsApiUser($user)->postJson('/api/v1/used-market/pc-builds/'.$build->id.'/estimate')->assertOk()->assertJsonStructure(['data' => ['estimatedPrice', 'partBreakdown']]);
        $this->getJson('/api/v1/auto/models')->assertOk()->assertJsonPath('data.0.name', 'PB E-Car');
        $this->postJson('/api/v1/auto/estimate', ['modelId' => $model->id, 'trimId' => $trim->id, 'optionIds' => [$option->id]])->assertOk()->assertJsonPath('data.optionPrice', 1200000);
    }

    public function test_auction_and_compare_flow(): void
    {
        $owner = $this->createUser('USER');
        $sellerUser = $this->createUser('USER');
        $category = $this->createCategory();
        $productA = $this->createProduct($category->id, 'PB Laptop', 'pb-laptop');
        $productB = $this->createProduct($category->id, 'PB Tablet', 'pb-tablet');

        $auction = $this->actingAsApiUser($owner)->postJson('/api/v1/auctions', [
            'title' => '노트북 구매 요청',
            'description' => '가성비 견적',
            'categoryId' => $category->id,
            'specs' => ['cpu' => 'i7'],
            'budget' => 1500000,
        ]);
        $auction->assertCreated();
        $auctionId = $auction->json('data.id');

        $bid = $this->actingAsApiUser($sellerUser)->postJson('/api/v1/auctions/'.$auctionId.'/bids', [
            'price' => 1420000,
            'description' => '3일 내 배송',
            'deliveryDays' => 3,
        ]);
        $bid->assertCreated();
        $bidId = $bid->json('data.id');

        $this->actingAsApiUser($owner)->patchJson('/api/v1/auctions/'.$auctionId.'/bids/'.$bidId.'/select')->assertOk()->assertJsonPath('data.message', '낙찰을 선택했습니다.');
        $this->getJson('/api/v1/auctions/'.$auctionId)->assertOk()->assertJsonPath('data.status', 'CLOSED');

        $headers = ['X-Compare-Key' => 'test-compare'];
        $this->withHeaders($headers)->postJson('/api/v1/compare/add', ['productId' => $productA->id])->assertOk();
        $this->withHeaders($headers)->postJson('/api/v1/compare/add', ['productId' => $productB->id])->assertOk();
        $this->withHeaders($headers)->getJson('/api/v1/compare')->assertOk()->assertJsonCount(2, 'data.compareList');
        $this->withHeaders($headers)->getJson('/api/v1/compare/detail')->assertOk()->assertJsonCount(2, 'data.items');
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('auc-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'PB User',
            'nickname' => uniqid('pb-', false),
            'role' => $role,
            'status' => 'ACTIVE',
            'phone' => '01012345678',
            'email_verified_at' => now(),
        ]);
    }

    private function createCategory(): Category
    {
        return Category::query()->create(['name' => '전자', 'slug' => uniqid('electronics-', true), 'depth' => 0, 'sort_order' => 0, 'is_visible' => true]);
    }

    private function createProduct(int $categoryId, string $name, string $slug): Product
    {
        return Product::query()->create(['category_id' => $categoryId, 'name' => $name, 'slug' => $slug.'-'.uniqid(), 'status' => 'ACTIVE']);
    }

    private function createSeller(): Seller
    {
        return Seller::query()->create(['name' => 'PB Seller', 'code' => uniqid('seller-', false), 'status' => 'ACTIVE', 'rating' => 4.5]);
    }

    private function actingAsApiUser(User $user): self
    {
        $token = app(JwtService::class)->createAccessToken($user);

        return $this->withHeader('Authorization', 'Bearer '.$token);
    }
}
