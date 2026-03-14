<?php

namespace App\Modules\Support\Requests;

use App\Http\Requests\ApiRequest;

class UpdateSupportTicketStatusRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'status' => ['required', 'string', 'max:40'],
        ];
    }
}
