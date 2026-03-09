<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class SearchIndexOutbox extends Model
{
    protected $table = 'search_index_outbox';

    protected $fillable = ['entity_type', 'entity_id', 'status', 'retry_count'];
}
