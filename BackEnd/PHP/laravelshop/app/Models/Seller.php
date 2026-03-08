<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Seller extends Model
{
    protected $fillable = [
        'name',
        'code',
        'status',
        'rating',
        'contact_email',
        'homepage_url',
    ];
}
