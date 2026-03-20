<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ProductMapping extends Model
{
    protected $fillable = ['source_name', 'product_id', 'status', 'reason'];
}
