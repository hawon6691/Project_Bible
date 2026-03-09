<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class News extends Model
{
    protected $table = 'news';

    protected $fillable = ['category_id', 'author_id', 'title', 'content', 'thumbnail_url'];
}
