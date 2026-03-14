<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class CrawlerJob extends Model
{
    protected $fillable = ['name', 'job_type', 'status', 'payload'];

    protected function casts(): array
    {
        return ['payload' => 'array'];
    }
}
