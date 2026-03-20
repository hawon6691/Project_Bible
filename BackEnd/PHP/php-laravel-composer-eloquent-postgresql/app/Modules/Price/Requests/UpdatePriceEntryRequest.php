<?php

namespace App\Modules\Price\Requests;

use App\Http\Requests\ApiRequest;

class UpdatePriceEntryRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'price' => ['sometimes', 'numeric', 'min:0'],
            'shippingFee' => ['sometimes', 'numeric', 'min:0'],
            'stockStatus' => ['sometimes', 'string', 'max:30'],
            'isCardDiscount' => ['sometimes', 'boolean'],
            'isCashDiscount' => ['sometimes', 'boolean'],
        ];
    }
}
