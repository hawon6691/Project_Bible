<?php

namespace App\Modules\Order\Requests;

use App\Http\Requests\ApiRequest;

class CreateOrderRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'addressId' => ['required', 'integer', 'exists:addresses,id'],
            'items' => ['sometimes', 'array', 'min:1'],
            'items.*.productId' => ['required_with:items', 'integer', 'exists:products,id'],
            'items.*.sellerId' => ['required_with:items', 'integer', 'exists:sellers,id'],
            'items.*.quantity' => ['required_with:items', 'integer', 'min:1'],
            'items.*.selectedOptions' => ['sometimes', 'nullable', 'string', 'max:255'],
            'fromCart' => ['sometimes', 'boolean'],
            'cartItemIds' => ['sometimes', 'array'],
            'cartItemIds.*' => ['integer', 'exists:cart_items,id'],
            'usePoint' => ['sometimes', 'numeric', 'min:0'],
            'memo' => ['sometimes', 'nullable', 'string', 'max:255'],
        ];
    }
}
