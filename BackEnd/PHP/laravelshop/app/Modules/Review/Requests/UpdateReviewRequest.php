<?php

namespace App\Modules\Review\Requests;

use App\Http\Requests\ApiRequest;

class UpdateReviewRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'rating' => ['sometimes', 'integer', 'min:1', 'max:5'],
            'content' => ['sometimes', 'string'],
        ];
    }
}
