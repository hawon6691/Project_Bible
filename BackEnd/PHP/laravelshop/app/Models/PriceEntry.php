<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class PriceEntry extends Model
{
    protected $fillable = [
        'product_id',
        'seller_id',
        'price',
        'shipping_fee',
        'is_card_discount',
        'is_cash_discount',
        'stock_status',
        'collected_at',
    ];

    protected function casts(): array
    {
        return [
            'price' => 'float',
            'shipping_fee' => 'float',
            'is_card_discount' => 'boolean',
            'is_cash_discount' => 'boolean',
            'collected_at' => 'datetime',
        ];
    }

    public function product(): BelongsTo
    {
        return $this->belongsTo(Product::class);
    }

    public function seller(): BelongsTo
    {
        return $this->belongsTo(Seller::class);
    }
}
