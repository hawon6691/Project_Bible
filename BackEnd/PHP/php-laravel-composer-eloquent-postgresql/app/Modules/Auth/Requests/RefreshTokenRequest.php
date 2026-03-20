<?php

namespace App\Modules\Auth\Requests;

use App\Http\Requests\ApiRequest;

class RefreshTokenRequest extends ApiRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'refreshToken' => ['required', 'string'],
        ];
    }
}
