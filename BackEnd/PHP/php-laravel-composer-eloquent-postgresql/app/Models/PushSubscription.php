<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PushSubscription extends Model
{
    protected $fillable = [
        'user_id',
        'endpoint',
        'endpoint_hash',
        'p256dh_key',
        'auth_key',
        'expiration_time',
        'is_active',
    ];

    protected static function booted(): void
    {
        static::saving(function (PushSubscription $subscription): void {
            $subscription->endpoint_hash = hash('sha256', $subscription->endpoint);
        });
    }

    protected function casts(): array
    {
        return [
            'is_active' => 'boolean',
        ];
    }
}
