<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ExchangeRate extends Model
{
    protected $fillable = [
        'base_currency',
        'target_currency',
        'rate',
        'updated_at_exchange',
    ];

    protected function casts(): array
    {
        return [
            'rate' => 'float',
            'updated_at_exchange' => 'datetime',
        ];
    }
}
