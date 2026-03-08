<?php

namespace App\Modules\Order\Requests;

use App\Http\Requests\ApiRequest;

class ListOrdersRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'page' => ['sometimes', 'integer', 'min:1'],
            'limit' => ['sometimes', 'integer', 'min:1', 'max:100'],
        ];
    }
}
