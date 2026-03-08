<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Product extends Model
{
    protected $fillable = [
        'category_id',
        'name',
        'slug',
        'description',
        'brand',
        'status',
        'thumbnail_url',
        'rating_avg',
        'review_count',
    ];

    protected function casts(): array
    {
        return [
            'rating_avg' => 'float',
            'review_count' => 'integer',
        ];
    }

    public function category(): BelongsTo
    {
        return $this->belongsTo(Category::class);
    }

    public function specs(): HasMany
    {
        return $this->hasMany(ProductSpec::class)->orderBy('sort_order')->orderBy('id');
    }

    public function priceEntries(): HasMany
    {
        return $this->hasMany(PriceEntry::class)->orderBy('price')->orderBy('id');
    }
}
