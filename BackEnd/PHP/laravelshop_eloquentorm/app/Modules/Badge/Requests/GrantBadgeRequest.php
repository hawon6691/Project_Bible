<?php

namespace App\Modules\Badge\Requests;

use App\Http\Requests\ApiRequest;

class GrantBadgeRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'userId' => ['required', 'integer', 'exists:users,id'],
        ];
    }
}
