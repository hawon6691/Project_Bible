<?php

namespace App\Modules\Recommendation\Requests;

use App\Http\Requests\ApiRequest;

class StoreRecommendationRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'productId' => ['required', 'integer', 'exists:products,id'],
            'type' => ['sometimes', 'string', 'max:40'],
            'title' => ['nullable', 'string', 'max:255'],
            'reason' => ['nullable', 'string'],
            'score' => ['sometimes', 'integer', 'min:0'],
            'isActive' => ['sometimes', 'boolean'],
        ];
    }
}
