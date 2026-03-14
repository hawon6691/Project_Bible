<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class PcBuild extends Model
{
    protected $fillable = ['user_id', 'name', 'description', 'share_code', 'view_count'];

    public function parts(): HasMany
    {
        return $this->hasMany(PcBuildPart::class);
    }
}
