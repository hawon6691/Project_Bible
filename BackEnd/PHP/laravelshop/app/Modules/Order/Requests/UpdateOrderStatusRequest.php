<?php

namespace App\Modules\Order\Requests;

use App\Http\Requests\ApiRequest;

class UpdateOrderStatusRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'status' => ['required', 'string', 'max:40'],
        ];
    }
}
