<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ProductQueryView extends Model
{
    protected $fillable = ['product_id', 'view_count', 'search_keywords'];

    protected function casts(): array
    {
        return ['search_keywords' => 'array'];
    }
}
