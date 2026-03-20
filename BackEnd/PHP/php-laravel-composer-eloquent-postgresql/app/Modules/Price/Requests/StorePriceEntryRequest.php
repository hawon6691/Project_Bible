<?php

namespace App\Modules\Price\Requests;

use App\Http\Requests\ApiRequest;

class StorePriceEntryRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'sellerId' => ['required', 'integer', 'exists:sellers,id'],
            'price' => ['required', 'numeric', 'min:0'],
            'shippingFee' => ['sometimes', 'numeric', 'min:0'],
            'stockStatus' => ['sometimes', 'string', 'max:30'],
            'isCardDiscount' => ['sometimes', 'boolean'],
            'isCashDiscount' => ['sometimes', 'boolean'],
        ];
    }
}
