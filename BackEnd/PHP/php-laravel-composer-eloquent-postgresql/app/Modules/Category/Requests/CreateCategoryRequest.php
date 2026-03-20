<?php

namespace App\Modules\Category\Requests;

use App\Http\Requests\ApiRequest;

class CreateCategoryRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'name' => ['required', 'string', 'max:120'],
            'parentId' => ['sometimes', 'nullable', 'integer', 'exists:categories,id'],
            'sortOrder' => ['sometimes', 'integer', 'min:0'],
            'isVisible' => ['sometimes', 'boolean'],
        ];
    }
}
