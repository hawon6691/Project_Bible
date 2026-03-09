<?php

namespace App\Modules\Push\Requests;

use App\Http\Requests\ApiRequest;

class RegisterPushSubscriptionRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'endpoint' => ['required', 'string', 'max:1000'],
            'p256dhKey' => ['required', 'string', 'max:255'],
            'authKey' => ['required', 'string', 'max:255'],
            'expirationTime' => ['nullable', 'string', 'max:50'],
        ];
    }
}
