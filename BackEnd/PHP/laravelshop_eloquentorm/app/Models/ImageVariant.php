<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ImageVariant extends Model
{
    protected $fillable = [
        'image_asset_id',
        'type',
        'path',
        'url',
        'width',
        'height',
        'format',
        'size',
    ];

    protected function casts(): array
    {
        return [
            'width' => 'integer',
            'height' => 'integer',
            'size' => 'integer',
        ];
    }
}
