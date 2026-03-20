<?php

namespace App\Modules\Support\Requests;

use App\Http\Requests\ApiRequest;

class StoreSupportTicketRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'category' => ['required', 'string', 'max:40'],
            'title' => ['required', 'string', 'max:255'],
            'content' => ['required', 'string'],
        ];
    }
}
