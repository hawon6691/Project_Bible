<?php

namespace App\Modules\Product\Requests;

use App\Http\Requests\ApiRequest;

class UpdateProductRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'categoryId' => ['sometimes', 'integer', 'exists:categories,id'],
            'name' => ['sometimes', 'string', 'max:255'],
            'description' => ['sometimes', 'nullable', 'string'],
            'brand' => ['sometimes', 'nullable', 'string', 'max:120'],
            'status' => ['sometimes', 'string', 'max:30'],
            'thumbnailUrl' => ['sometimes', 'nullable', 'string', 'max:500'],
        ];
    }
}
