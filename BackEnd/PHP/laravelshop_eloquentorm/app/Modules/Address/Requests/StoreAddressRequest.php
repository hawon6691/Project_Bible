<?php

namespace App\Modules\Address\Requests;

use App\Http\Requests\ApiRequest;

class StoreAddressRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'recipientName' => ['required', 'string', 'max:120'],
            'label' => ['sometimes', 'nullable', 'string', 'max:80'],
            'phone' => ['required', 'string', 'max:30'],
            'zipCode' => ['required', 'string', 'max:20'],
            'addressLine1' => ['required', 'string', 'max:255'],
            'addressLine2' => ['sometimes', 'nullable', 'string', 'max:255'],
            'memo' => ['sometimes', 'nullable', 'string', 'max:255'],
            'isDefault' => ['sometimes', 'boolean'],
        ];
    }
}
