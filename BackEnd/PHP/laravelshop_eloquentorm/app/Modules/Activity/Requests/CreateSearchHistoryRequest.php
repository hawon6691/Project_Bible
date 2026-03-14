<?php

namespace App\Modules\Activity\Requests;

use App\Http\Requests\ApiRequest;

class CreateSearchHistoryRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'keyword' => ['required', 'string', 'max:100'],
        ];
    }
}
