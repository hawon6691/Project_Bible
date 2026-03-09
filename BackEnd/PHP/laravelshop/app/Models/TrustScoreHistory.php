<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class TrustScoreHistory extends Model
{
    protected $fillable = [
        'seller_id',
        'score',
        'grade',
        'trend',
        'breakdown',
        'recorded_at',
    ];

    protected function casts(): array
    {
        return [
            'score' => 'integer',
            'breakdown' => 'array',
            'recorded_at' => 'datetime',
        ];
    }
}
