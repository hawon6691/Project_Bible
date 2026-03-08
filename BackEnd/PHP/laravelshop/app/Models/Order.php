<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Order extends Model
{
    protected $fillable = [
        'user_id',
        'address_id',
        'order_number',
        'status',
        'total_amount',
        'point_used',
        'final_amount',
        'recipient_name',
        'phone',
        'zip_code',
        'address_line1',
        'address_line2',
        'memo',
        'cancelled_at',
    ];

    protected function casts(): array
    {
        return [
            'total_amount' => 'float',
            'point_used' => 'float',
            'final_amount' => 'float',
            'cancelled_at' => 'datetime',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function address(): BelongsTo
    {
        return $this->belongsTo(Address::class);
    }

    public function items(): HasMany
    {
        return $this->hasMany(OrderItem::class);
    }

    public function payments(): HasMany
    {
        return $this->hasMany(Payment::class);
    }
}
