<?php

namespace App\Modules\Payment\Requests;

use App\Http\Requests\ApiRequest;

class CreatePaymentRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'orderId' => ['required', 'integer', 'exists:orders,id'],
            'method' => ['sometimes', 'string', 'max:30'],
            'provider' => ['sometimes', 'nullable', 'string', 'max:60'],
        ];
    }
}
