<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('pc_builds', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->string('name', 120);
            $table->string('description', 255)->nullable();
            $table->string('share_code', 40)->nullable()->unique();
            $table->unsignedInteger('view_count')->default(0);
            $table->timestamps();
        });

        Schema::create('pc_build_parts', function (Blueprint $table) {
            $table->id();
            $table->foreignId('pc_build_id')->constrained('pc_builds')->cascadeOnDelete();
            $table->string('part_type', 40);
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->unsignedInteger('quantity')->default(1);
            $table->timestamps();
        });

        Schema::create('pc_compatibility_rules', function (Blueprint $table) {
            $table->id();
            $table->string('name', 120);
            $table->string('source_part_type', 40);
            $table->string('target_part_type', 40);
            $table->string('rule_type', 40);
            $table->json('rule_value')->nullable();
            $table->timestamps();
        });

        Schema::create('friendships', function (Blueprint $table) {
            $table->id();
            $table->foreignId('requester_id')->constrained('users')->cascadeOnDelete();
            $table->foreignId('addressee_id')->constrained('users')->cascadeOnDelete();
            $table->string('status', 20)->default('PENDING');
            $table->timestamps();
        });

        Schema::create('friend_blocks', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->foreignId('blocked_user_id')->constrained('users')->cascadeOnDelete();
            $table->timestamps();
            $table->unique(['user_id', 'blocked_user_id']);
        });

        Schema::create('friend_activities', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->string('type', 40);
            $table->string('message', 255);
            $table->timestamps();
        });

        Schema::create('shortforms', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->string('title', 255);
            $table->string('video_url', 500);
            $table->string('thumbnail_url', 500)->nullable();
            $table->unsignedInteger('view_count')->default(0);
            $table->unsignedInteger('like_count')->default(0);
            $table->unsignedInteger('comment_count')->default(0);
            $table->string('transcode_status', 40)->default('COMPLETED');
            $table->timestamps();
        });

        Schema::create('shortform_products', function (Blueprint $table) {
            $table->id();
            $table->foreignId('shortform_id')->constrained('shortforms')->cascadeOnDelete();
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->timestamps();
        });

        Schema::create('shortform_likes', function (Blueprint $table) {
            $table->id();
            $table->foreignId('shortform_id')->constrained('shortforms')->cascadeOnDelete();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->timestamps();
            $table->unique(['shortform_id', 'user_id']);
        });

        Schema::create('shortform_comments', function (Blueprint $table) {
            $table->id();
            $table->foreignId('shortform_id')->constrained('shortforms')->cascadeOnDelete();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->text('content');
            $table->timestamps();
        });

        Schema::create('media_assets', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->string('owner_type', 40);
            $table->unsignedBigInteger('owner_id')->nullable();
            $table->string('file_name', 255);
            $table->string('file_path', 500);
            $table->string('file_url', 500);
            $table->string('mime_type', 120);
            $table->unsignedBigInteger('size');
            $table->timestamps();
        });

        Schema::create('news_categories', function (Blueprint $table) {
            $table->id();
            $table->string('name', 120);
            $table->string('slug', 120)->unique();
            $table->timestamps();
        });

        Schema::create('news', function (Blueprint $table) {
            $table->id();
            $table->foreignId('category_id')->nullable()->constrained('news_categories')->nullOnDelete();
            $table->foreignId('author_id')->constrained('users')->cascadeOnDelete();
            $table->string('title', 255);
            $table->text('content');
            $table->string('thumbnail_url', 500)->nullable();
            $table->timestamps();
        });

        Schema::create('news_products', function (Blueprint $table) {
            $table->id();
            $table->foreignId('news_id')->constrained('news')->cascadeOnDelete();
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->timestamps();
        });

        Schema::create('product_mappings', function (Blueprint $table) {
            $table->id();
            $table->string('source_name', 255);
            $table->foreignId('product_id')->nullable()->constrained('products')->nullOnDelete();
            $table->string('status', 20)->default('PENDING');
            $table->string('reason', 255)->nullable();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('product_mappings');
        Schema::dropIfExists('news_products');
        Schema::dropIfExists('news');
        Schema::dropIfExists('news_categories');
        Schema::dropIfExists('media_assets');
        Schema::dropIfExists('shortform_comments');
        Schema::dropIfExists('shortform_likes');
        Schema::dropIfExists('shortform_products');
        Schema::dropIfExists('shortforms');
        Schema::dropIfExists('friend_activities');
        Schema::dropIfExists('friend_blocks');
        Schema::dropIfExists('friendships');
        Schema::dropIfExists('pc_compatibility_rules');
        Schema::dropIfExists('pc_build_parts');
        Schema::dropIfExists('pc_builds');
    }
};
