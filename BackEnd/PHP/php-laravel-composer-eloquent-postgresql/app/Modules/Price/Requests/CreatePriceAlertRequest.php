<?php

namespace App\Modules\Price\Requests;

use App\Http\Requests\ApiRequest;

class CreatePriceAlertRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'productId' => ['required', 'integer', 'exists:products,id'],
            'targetPrice' => ['required', 'numeric', 'min:0'],
        ];
    }
}
