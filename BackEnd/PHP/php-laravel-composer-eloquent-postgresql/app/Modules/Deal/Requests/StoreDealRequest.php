<?php

namespace App\Modules\Deal\Requests;

use App\Http\Requests\ApiRequest;

class StoreDealRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'productId' => ['required', 'integer', 'exists:products,id'],
            'title' => ['required', 'string', 'max:255'],
            'type' => ['sometimes', 'string', 'max:40'],
            'description' => ['nullable', 'string'],
            'dealPrice' => ['required', 'numeric', 'min:0'],
            'discountRate' => ['sometimes', 'numeric', 'min:0'],
            'stock' => ['sometimes', 'integer', 'min:0'],
            'startAt' => ['required', 'date'],
            'endAt' => ['required', 'date', 'after:startAt'],
        ];
    }
}
