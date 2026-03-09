<?php

namespace App\Modules\Chat\Requests;

use App\Http\Requests\ApiRequest;

class SendChatMessageRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'message' => ['required', 'string', 'max:1000'],
        ];
    }
}
