<?php

namespace App\Modules\Point\Requests;

use App\Http\Requests\ApiRequest;

class ListPointTransactionsRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'page' => ['sometimes', 'integer', 'min:1'],
            'limit' => ['sometimes', 'integer', 'min:1', 'max:100'],
            'type' => ['sometimes', 'string', 'max:30'],
        ];
    }
}
