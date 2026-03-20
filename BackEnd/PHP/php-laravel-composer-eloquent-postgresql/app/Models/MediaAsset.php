<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class MediaAsset extends Model
{
    protected $fillable = ['user_id', 'owner_type', 'owner_id', 'file_name', 'file_path', 'file_url', 'mime_type', 'size'];
}
