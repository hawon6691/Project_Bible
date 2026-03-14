<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class AutoLeaseOffer extends Model
{
    protected $fillable = ['auto_model_id', 'provider', 'monthly_payment', 'contract_months'];
}
