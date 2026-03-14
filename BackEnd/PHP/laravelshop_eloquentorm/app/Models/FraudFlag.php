<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class FraudFlag extends Model
{
    protected $fillable = [
        'product_id',
        'price_entry_id',
        'status',
        'reason',
        'detected_price',
        'baseline_price',
        'approved_by',
        'rejected_by',
        'approved_at',
        'rejected_at',
    ];

    protected function casts(): array
    {
        return [
            'detected_price' => 'float',
            'baseline_price' => 'float',
            'approved_at' => 'datetime',
            'rejected_at' => 'datetime',
        ];
    }
}
