<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class AuctionBid extends Model
{
    protected $fillable = ['auction_id', 'seller_id', 'price', 'description', 'delivery_days'];
}
