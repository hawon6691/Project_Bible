<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('crawler_jobs', function (Blueprint $table) {
            $table->id();
            $table->string('name', 120);
            $table->string('job_type', 40);
            $table->string('status', 20)->default('ACTIVE');
            $table->json('payload')->nullable();
            $table->timestamps();
        });

        Schema::create('crawler_runs', function (Blueprint $table) {
            $table->id();
            $table->foreignId('crawler_job_id')->nullable()->constrained('crawler_jobs')->nullOnDelete();
            $table->string('status', 20)->default('QUEUED');
            $table->unsignedInteger('collected_count')->default(0);
            $table->string('trigger_type', 40)->default('MANUAL');
            $table->timestamps();
        });

        Schema::create('search_index_outbox', function (Blueprint $table) {
            $table->id();
            $table->string('entity_type', 40);
            $table->unsignedBigInteger('entity_id');
            $table->string('status', 20)->default('PENDING');
            $table->unsignedInteger('retry_count')->default(0);
            $table->timestamps();
        });

        Schema::create('product_query_views', function (Blueprint $table) {
            $table->id();
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->unsignedInteger('view_count')->default(0);
            $table->json('search_keywords')->nullable();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('product_query_views');
        Schema::dropIfExists('search_index_outbox');
        Schema::dropIfExists('crawler_runs');
        Schema::dropIfExists('crawler_jobs');
    }
};
