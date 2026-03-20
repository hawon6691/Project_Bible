<?php

namespace App\Modules\Point\Requests;

use App\Http\Requests\ApiRequest;

class AdminGrantPointRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'userId' => ['required', 'integer', 'exists:users,id'],
            'amount' => ['required', 'numeric', 'min:1'],
            'description' => ['required', 'string', 'max:255'],
        ];
    }
}
