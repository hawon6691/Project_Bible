<?php

namespace App\Modules\Auth\Requests;

use App\Http\Requests\ApiRequest;

class PasswordResetConfirmRequest extends ApiRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'resetToken' => ['required', 'string'],
            'newPassword' => ['required', 'string', 'min:8'],
        ];
    }
}
