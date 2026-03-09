<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Deal extends Model
{
    protected $fillable = [
        'product_id',
        'title',
        'type',
        'description',
        'deal_price',
        'discount_rate',
        'stock',
        'start_at',
        'end_at',
    ];

    protected function casts(): array
    {
        return [
            'deal_price' => 'float',
            'discount_rate' => 'float',
            'stock' => 'integer',
            'start_at' => 'datetime',
            'end_at' => 'datetime',
        ];
    }

    public function product(): BelongsTo
    {
        return $this->belongsTo(Product::class);
    }
}
