<?php

namespace App\Modules\Push\Requests;

use App\Http\Requests\ApiRequest;

class UpdatePushPreferenceRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'priceAlertEnabled' => ['sometimes', 'boolean'],
            'orderStatusEnabled' => ['sometimes', 'boolean'],
            'chatMessageEnabled' => ['sometimes', 'boolean'],
            'dealEnabled' => ['sometimes', 'boolean'],
        ];
    }
}
