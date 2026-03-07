<?php

namespace App\Modules\Auth\Requests;

use App\Http\Requests\ApiRequest;

class PasswordResetVerifyRequest extends ApiRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'email' => ['required', 'email'],
            'code' => ['required', 'digits:6'],
        ];
    }
}
