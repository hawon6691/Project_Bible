<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Shortform extends Model
{
    protected $fillable = ['user_id', 'title', 'video_url', 'thumbnail_url', 'view_count', 'like_count', 'comment_count', 'transcode_status'];

    public function comments(): HasMany
    {
        return $this->hasMany(ShortformComment::class);
    }
}
