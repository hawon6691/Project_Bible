<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class FriendActivity extends Model
{
    protected $fillable = ['user_id', 'type', 'message'];
}
