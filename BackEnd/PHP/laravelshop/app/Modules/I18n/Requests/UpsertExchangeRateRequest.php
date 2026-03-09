<?php

namespace App\Modules\I18n\Requests;

use App\Http\Requests\ApiRequest;

class UpsertExchangeRateRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'baseCurrency' => ['required', 'string', 'max:10'],
            'targetCurrency' => ['required', 'string', 'max:10'],
            'rate' => ['required', 'numeric', 'min:0'],
        ];
    }
}
