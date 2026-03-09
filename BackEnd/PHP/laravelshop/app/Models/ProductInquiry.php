<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ProductInquiry extends Model
{
    protected $fillable = [
        'product_id',
        'user_id',
        'title',
        'content',
        'is_secret',
        'answer',
        'answered_by',
        'answered_at',
    ];

    protected function casts(): array
    {
        return [
            'is_secret' => 'boolean',
            'answered_at' => 'datetime',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
