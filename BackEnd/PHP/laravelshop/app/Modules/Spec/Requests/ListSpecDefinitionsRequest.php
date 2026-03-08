<?php

namespace App\Modules\Spec\Requests;

use App\Http\Requests\ApiRequest;

class ListSpecDefinitionsRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'categoryId' => ['sometimes', 'integer', 'exists:categories,id'],
        ];
    }
}
