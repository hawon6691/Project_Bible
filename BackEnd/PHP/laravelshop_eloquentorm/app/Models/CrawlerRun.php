<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class CrawlerRun extends Model
{
    protected $fillable = ['crawler_job_id', 'status', 'collected_count', 'trigger_type'];
}
