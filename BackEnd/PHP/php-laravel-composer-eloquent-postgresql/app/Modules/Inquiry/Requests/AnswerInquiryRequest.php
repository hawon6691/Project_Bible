<?php

namespace App\Modules\Inquiry\Requests;

use App\Http\Requests\ApiRequest;

class AnswerInquiryRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'answer' => ['required', 'string'],
        ];
    }
}
