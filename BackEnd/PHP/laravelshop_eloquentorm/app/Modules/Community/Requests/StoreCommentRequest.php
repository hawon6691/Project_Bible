<?php

namespace App\Modules\Community\Requests;

use App\Http\Requests\ApiRequest;

class StoreCommentRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'content' => ['required', 'string'],
        ];
    }
}
