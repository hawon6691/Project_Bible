<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PushPreference extends Model
{
    protected $fillable = [
        'user_id',
        'price_alert_enabled',
        'order_status_enabled',
        'chat_message_enabled',
        'deal_enabled',
    ];

    protected function casts(): array
    {
        return [
            'price_alert_enabled' => 'boolean',
            'order_status_enabled' => 'boolean',
            'chat_message_enabled' => 'boolean',
            'deal_enabled' => 'boolean',
        ];
    }
}
