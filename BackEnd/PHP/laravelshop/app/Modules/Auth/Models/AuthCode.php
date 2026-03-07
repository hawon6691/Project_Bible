<?php

namespace App\Modules\Auth\Models;

use Illuminate\Database\Eloquent\Model;

class AuthCode extends Model
{
    protected $fillable = [
        'user_id',
        'email',
        'purpose',
        'code',
        'expires_at',
        'verified_at',
    ];

    protected function casts(): array
    {
        return [
            'expires_at' => 'datetime',
            'verified_at' => 'datetime',
        ];
    }
}
