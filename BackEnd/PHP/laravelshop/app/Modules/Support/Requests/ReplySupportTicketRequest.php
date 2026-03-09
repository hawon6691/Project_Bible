<?php

namespace App\Modules\Support\Requests;

use App\Http\Requests\ApiRequest;

class ReplySupportTicketRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'content' => ['required', 'string'],
        ];
    }
}
