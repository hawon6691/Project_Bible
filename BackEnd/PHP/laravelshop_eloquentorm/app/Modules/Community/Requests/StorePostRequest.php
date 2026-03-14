<?php

namespace App\Modules\Community\Requests;

use App\Http\Requests\ApiRequest;

class StorePostRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'title' => ['required', 'string', 'max:255'],
            'content' => ['required', 'string'],
        ];
    }
}
