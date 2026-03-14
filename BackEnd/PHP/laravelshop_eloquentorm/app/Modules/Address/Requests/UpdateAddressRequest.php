<?php

namespace App\Modules\Address\Requests;

use App\Http\Requests\ApiRequest;

class UpdateAddressRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'recipientName' => ['sometimes', 'string', 'max:120'],
            'label' => ['sometimes', 'nullable', 'string', 'max:80'],
            'phone' => ['sometimes', 'string', 'max:30'],
            'zipCode' => ['sometimes', 'string', 'max:20'],
            'addressLine1' => ['sometimes', 'string', 'max:255'],
            'addressLine2' => ['sometimes', 'nullable', 'string', 'max:255'],
            'memo' => ['sometimes', 'nullable', 'string', 'max:255'],
            'isDefault' => ['sometimes', 'boolean'],
        ];
    }
}
