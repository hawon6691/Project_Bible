<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PcCompatibilityRule extends Model
{
    protected $fillable = ['name', 'source_part_type', 'target_part_type', 'rule_type', 'rule_value'];

    protected function casts(): array
    {
        return ['rule_value' => 'array'];
    }
}
