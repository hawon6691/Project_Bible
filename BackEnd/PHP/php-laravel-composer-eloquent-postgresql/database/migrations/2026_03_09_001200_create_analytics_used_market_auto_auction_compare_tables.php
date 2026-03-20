<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('used_market_prices', function (Blueprint $table) {
            $table->id();
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->decimal('price', 12, 2);
            $table->timestamps();
        });

        Schema::create('auto_models', function (Blueprint $table) {
            $table->id();
            $table->string('brand', 120);
            $table->string('name', 120);
            $table->string('type', 40)->default('CAR');
            $table->timestamps();
        });

        Schema::create('auto_trims', function (Blueprint $table) {
            $table->id();
            $table->foreignId('auto_model_id')->constrained('auto_models')->cascadeOnDelete();
            $table->string('name', 120);
            $table->decimal('base_price', 12, 2);
            $table->timestamps();
        });

        Schema::create('auto_options', function (Blueprint $table) {
            $table->id();
            $table->foreignId('auto_trim_id')->constrained('auto_trims')->cascadeOnDelete();
            $table->string('name', 120);
            $table->decimal('price', 12, 2);
            $table->timestamps();
        });

        Schema::create('auto_lease_offers', function (Blueprint $table) {
            $table->id();
            $table->foreignId('auto_model_id')->constrained('auto_models')->cascadeOnDelete();
            $table->string('provider', 120);
            $table->decimal('monthly_payment', 12, 2);
            $table->unsignedInteger('contract_months');
            $table->timestamps();
        });

        Schema::create('auctions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->foreignId('category_id')->nullable()->constrained('categories')->nullOnDelete();
            $table->string('title', 255);
            $table->text('description')->nullable();
            $table->json('specs')->nullable();
            $table->decimal('budget', 12, 2)->nullable();
            $table->string('status', 20)->default('OPEN');
            $table->unsignedBigInteger('selected_bid_id')->nullable();
            $table->timestamps();
        });

        Schema::create('auction_bids', function (Blueprint $table) {
            $table->id();
            $table->foreignId('auction_id')->constrained('auctions')->cascadeOnDelete();
            $table->foreignId('seller_id')->constrained('sellers')->cascadeOnDelete();
            $table->decimal('price', 12, 2);
            $table->text('description')->nullable();
            $table->unsignedInteger('delivery_days')->nullable();
            $table->timestamps();
        });

        Schema::create('compare_items', function (Blueprint $table) {
            $table->id();
            $table->string('compare_key', 80);
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->timestamps();
            $table->unique(['compare_key', 'product_id']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('compare_items');
        Schema::dropIfExists('auction_bids');
        Schema::dropIfExists('auctions');
        Schema::dropIfExists('auto_lease_offers');
        Schema::dropIfExists('auto_options');
        Schema::dropIfExists('auto_trims');
        Schema::dropIfExists('auto_models');
        Schema::dropIfExists('used_market_prices');
    }
};
