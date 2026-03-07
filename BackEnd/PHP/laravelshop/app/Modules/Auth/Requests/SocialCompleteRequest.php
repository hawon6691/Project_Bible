<?php

namespace App\Modules\Auth\Requests;

use App\Http\Requests\ApiRequest;

class SocialCompleteRequest extends ApiRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'phone' => ['required', 'string', 'max:30'],
            'nickname' => ['required', 'string', 'max:100'],
        ];
    }
}
