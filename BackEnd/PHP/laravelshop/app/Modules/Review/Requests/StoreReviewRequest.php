<?php

namespace App\Modules\Review\Requests;

use App\Http\Requests\ApiRequest;

class StoreReviewRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'orderId' => ['required', 'integer', 'exists:orders,id'],
            'rating' => ['required', 'integer', 'min:1', 'max:5'],
            'content' => ['required', 'string'],
        ];
    }
}
