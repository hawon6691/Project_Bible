<?php

namespace App\Modules\User\Requests;

use App\Http\Requests\ApiRequest;

class UpdateProfileRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'nickname' => ['sometimes', 'nullable', 'string', 'max:100'],
            'bio' => ['sometimes', 'nullable', 'string', 'max:1000'],
        ];
    }
}
