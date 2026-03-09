<?php

namespace Tests\E2E;

use App\Models\Category;
use App\Models\Product;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ContractPublicApiE2ETest extends TestCase
{
    use RefreshDatabase;

    public function test_public_contract_endpoints_are_available(): void
    {
        $category = Category::query()->create([
            'name' => '계약 카테고리',
            'slug' => 'contract-category',
            'depth' => 0,
            'sort_order' => 1,
            'is_visible' => true,
        ]);

        Product::query()->create([
            'category_id' => $category->id,
            'name' => 'Contract Product',
            'slug' => 'contract-product',
            'status' => 'ACTIVE',
        ]);

        $this->getJson('/api/v1/health')
            ->assertOk()
            ->assertJsonPath('success', true);

        $this->getJson('/api/v1/categories')
            ->assertOk()
            ->assertJsonPath('success', true);

        $this->getJson('/api/v1/products')
            ->assertOk()
            ->assertJsonPath('success', true);
    }
}
