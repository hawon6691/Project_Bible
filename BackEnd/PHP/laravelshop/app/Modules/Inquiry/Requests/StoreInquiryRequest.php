<?php

namespace App\Modules\Inquiry\Requests;

use App\Http\Requests\ApiRequest;

class StoreInquiryRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'title' => ['required', 'string', 'max:255'],
            'content' => ['required', 'string'],
            'isSecret' => ['sometimes', 'boolean'],
        ];
    }
}
