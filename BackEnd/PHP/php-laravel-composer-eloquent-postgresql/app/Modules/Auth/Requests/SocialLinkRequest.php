<?php

namespace App\Modules\Auth\Requests;

use App\Http\Requests\ApiRequest;

class SocialLinkRequest extends ApiRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'provider' => ['required', 'string'],
            'socialToken' => ['required', 'string'],
        ];
    }
}
