<?php

namespace App\Modules\Chat\Requests;

use App\Http\Requests\ApiRequest;

class CreateChatRoomRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'name' => ['required', 'string', 'max:100'],
            'isPrivate' => ['sometimes', 'boolean'],
        ];
    }
}
