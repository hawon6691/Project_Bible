<?php

namespace App\Modules\User\Requests;

use App\Http\Requests\ApiRequest;

class UpdateMeRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'name' => ['sometimes', 'string', 'max:100'],
            'phone' => ['sometimes', 'nullable', 'string', 'max:30'],
            'password' => ['sometimes', 'string', 'min:8', 'max:255'],
        ];
    }
}
