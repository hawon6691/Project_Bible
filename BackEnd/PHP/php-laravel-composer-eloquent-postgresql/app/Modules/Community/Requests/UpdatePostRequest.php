<?php

namespace App\Modules\Community\Requests;

use App\Http\Requests\ApiRequest;

class UpdatePostRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'title' => ['sometimes', 'string', 'max:255'],
            'content' => ['sometimes', 'string'],
        ];
    }
}
