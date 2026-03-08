<?php

namespace App\Modules\Category\Requests;

use App\Http\Requests\ApiRequest;

class UpdateCategoryRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'name' => ['sometimes', 'string', 'max:120'],
            'sortOrder' => ['sometimes', 'integer', 'min:0'],
            'isVisible' => ['sometimes', 'boolean'],
        ];
    }
}
