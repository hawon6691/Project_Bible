<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class ImageAsset extends Model
{
    protected $fillable = [
        'user_id',
        'category',
        'original_name',
        'original_path',
        'original_url',
        'mime_type',
        'size',
        'processing_status',
    ];

    protected function casts(): array
    {
        return [
            'size' => 'integer',
        ];
    }

    public function variants(): HasMany
    {
        return $this->hasMany(ImageVariant::class);
    }
}
