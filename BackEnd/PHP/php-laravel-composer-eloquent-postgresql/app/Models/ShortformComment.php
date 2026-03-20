<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ShortformComment extends Model
{
    protected $fillable = ['shortform_id', 'user_id', 'content'];
}
