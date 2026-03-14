<?php

namespace App\Modules\Push\Requests;

use App\Http\Requests\ApiRequest;

class UnregisterPushSubscriptionRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'endpoint' => ['required', 'string', 'max:1000'],
        ];
    }
}
