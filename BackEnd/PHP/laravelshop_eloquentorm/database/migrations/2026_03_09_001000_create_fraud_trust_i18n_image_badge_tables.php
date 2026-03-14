<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('fraud_flags', function (Blueprint $table) {
            $table->id();
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->foreignId('price_entry_id')->nullable()->constrained('price_entries')->nullOnDelete();
            $table->string('status', 40)->default('PENDING');
            $table->string('reason', 255);
            $table->decimal('detected_price', 15, 2);
            $table->decimal('baseline_price', 15, 2)->default(0);
            $table->foreignId('approved_by')->nullable()->constrained('users')->nullOnDelete();
            $table->foreignId('rejected_by')->nullable()->constrained('users')->nullOnDelete();
            $table->timestamp('approved_at')->nullable();
            $table->timestamp('rejected_at')->nullable();
            $table->timestamps();
        });

        Schema::create('trust_score_histories', function (Blueprint $table) {
            $table->id();
            $table->foreignId('seller_id')->constrained('sellers')->cascadeOnDelete();
            $table->integer('score');
            $table->string('grade', 10);
            $table->string('trend', 40)->default('STABLE');
            $table->json('breakdown')->nullable();
            $table->timestamp('recorded_at');
            $table->timestamps();
        });

        Schema::create('translations', function (Blueprint $table) {
            $table->id();
            $table->string('locale', 10);
            $table->string('namespace', 100);
            $table->string('key', 191);
            $table->text('value');
            $table->timestamps();

            $table->unique(['locale', 'namespace', 'key']);
        });

        Schema::create('exchange_rates', function (Blueprint $table) {
            $table->id();
            $table->string('base_currency', 10);
            $table->string('target_currency', 10);
            $table->decimal('rate', 18, 8);
            $table->timestamp('updated_at_exchange')->nullable();
            $table->timestamps();

            $table->unique(['base_currency', 'target_currency']);
        });

        Schema::create('image_assets', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->string('category', 40);
            $table->string('original_name', 255);
            $table->string('original_path', 500);
            $table->string('original_url', 500);
            $table->string('mime_type', 100);
            $table->unsignedBigInteger('size');
            $table->string('processing_status', 40)->default('COMPLETED');
            $table->timestamps();
        });

        Schema::create('image_variants', function (Blueprint $table) {
            $table->id();
            $table->foreignId('image_asset_id')->constrained('image_assets')->cascadeOnDelete();
            $table->string('type', 40);
            $table->string('path', 500);
            $table->string('url', 500);
            $table->integer('width')->nullable();
            $table->integer('height')->nullable();
            $table->string('format', 20);
            $table->unsignedBigInteger('size');
            $table->timestamps();
        });

        Schema::create('badges', function (Blueprint $table) {
            $table->id();
            $table->string('name', 100);
            $table->string('description', 255)->nullable();
            $table->string('icon_url', 500)->nullable();
            $table->string('type', 20)->default('AUTO');
            $table->json('condition')->nullable();
            $table->string('rarity', 20)->default('COMMON');
            $table->timestamps();
        });

        Schema::create('user_badges', function (Blueprint $table) {
            $table->id();
            $table->foreignId('badge_id')->constrained('badges')->cascadeOnDelete();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->foreignId('granted_by')->nullable()->constrained('users')->nullOnDelete();
            $table->timestamp('granted_at');
            $table->timestamps();

            $table->unique(['badge_id', 'user_id']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('user_badges');
        Schema::dropIfExists('badges');
        Schema::dropIfExists('image_variants');
        Schema::dropIfExists('image_assets');
        Schema::dropIfExists('exchange_rates');
        Schema::dropIfExists('translations');
        Schema::dropIfExists('trust_score_histories');
        Schema::dropIfExists('fraud_flags');
    }
};
