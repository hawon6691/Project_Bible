<?php

namespace App\Modules\Deal\Requests;

use App\Http\Requests\ApiRequest;

class UpdateDealRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'productId' => ['sometimes', 'integer', 'exists:products,id'],
            'title' => ['sometimes', 'string', 'max:255'],
            'type' => ['sometimes', 'string', 'max:40'],
            'description' => ['nullable', 'string'],
            'dealPrice' => ['sometimes', 'numeric', 'min:0'],
            'discountRate' => ['sometimes', 'numeric', 'min:0'],
            'stock' => ['sometimes', 'integer', 'min:0'],
            'startAt' => ['sometimes', 'date'],
            'endAt' => ['sometimes', 'date'],
        ];
    }
}
