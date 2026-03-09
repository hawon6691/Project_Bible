<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class SupportTicketReply extends Model
{
    protected $fillable = ['ticket_id', 'user_id', 'content'];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
