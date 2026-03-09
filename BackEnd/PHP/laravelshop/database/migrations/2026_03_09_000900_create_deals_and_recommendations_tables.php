<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('deals', function (Blueprint $table) {
            $table->id();
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->string('title', 255);
            $table->string('type', 40)->default('SPECIAL');
            $table->text('description')->nullable();
            $table->decimal('deal_price', 15, 2);
            $table->decimal('discount_rate', 5, 2)->default(0);
            $table->integer('stock')->default(0);
            $table->timestamp('start_at');
            $table->timestamp('end_at');
            $table->timestamps();
        });

        Schema::create('recommendations', function (Blueprint $table) {
            $table->id();
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->string('type', 40)->default('TODAY');
            $table->string('title', 255)->nullable();
            $table->text('reason')->nullable();
            $table->integer('score')->default(0);
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('recommendations');
        Schema::dropIfExists('deals');
    }
};
