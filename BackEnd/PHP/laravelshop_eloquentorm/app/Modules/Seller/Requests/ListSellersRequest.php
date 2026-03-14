<?php

namespace App\Modules\Seller\Requests;

use App\Http\Requests\ApiRequest;

class ListSellersRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'page' => ['sometimes', 'integer', 'min:1'],
            'limit' => ['sometimes', 'integer', 'min:1', 'max:100'],
        ];
    }
}
