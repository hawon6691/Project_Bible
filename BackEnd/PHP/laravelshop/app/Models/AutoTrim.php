<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class AutoTrim extends Model
{
    protected $fillable = ['auto_model_id', 'name', 'base_price'];
}
