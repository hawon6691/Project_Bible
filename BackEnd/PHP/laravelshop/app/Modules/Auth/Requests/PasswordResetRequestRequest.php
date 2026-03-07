<?php

namespace App\Modules\Auth\Requests;

use App\Http\Requests\ApiRequest;

class PasswordResetRequestRequest extends ApiRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'email' => ['required', 'email'],
            'phone' => ['required', 'string', 'max:30'],
        ];
    }
}
