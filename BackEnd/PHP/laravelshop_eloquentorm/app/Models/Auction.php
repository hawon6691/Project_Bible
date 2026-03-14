<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Auction extends Model
{
    protected $fillable = ['user_id', 'category_id', 'title', 'description', 'specs', 'budget', 'status', 'selected_bid_id'];

    protected function casts(): array
    {
        return ['specs' => 'array'];
    }
}
