<?php

namespace App\Modules\Cart\Requests;

use App\Http\Requests\ApiRequest;

class StoreCartItemRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'productId' => ['required', 'integer', 'exists:products,id'],
            'sellerId' => ['required', 'integer', 'exists:sellers,id'],
            'quantity' => ['required', 'integer', 'min:1'],
            'selectedOptions' => ['sometimes', 'nullable', 'string', 'max:255'],
        ];
    }
}
