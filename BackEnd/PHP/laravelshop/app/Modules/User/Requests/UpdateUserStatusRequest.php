<?php

namespace App\Modules\User\Requests;

use App\Http\Requests\ApiRequest;
use Illuminate\Validation\Rule;

class UpdateUserStatusRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'status' => ['required', 'string', Rule::in(['ACTIVE', 'INACTIVE', 'BLOCKED'])],
        ];
    }
}
