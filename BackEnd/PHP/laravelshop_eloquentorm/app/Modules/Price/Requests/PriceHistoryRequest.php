<?php

namespace App\Modules\Price\Requests;

use App\Http\Requests\ApiRequest;
use Illuminate\Validation\Rule;

class PriceHistoryRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'period' => ['sometimes', 'string', Rule::in(['1w', '1m', '3m', '6m', '1y'])],
            'type' => ['sometimes', 'string', Rule::in(['daily', 'weekly', 'monthly'])],
        ];
    }
}
