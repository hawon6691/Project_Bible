<?php

namespace App\Modules\Spec\Requests;

use App\Http\Requests\ApiRequest;

class StoreSpecDefinitionRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'categoryId' => ['required', 'integer', 'exists:categories,id'],
            'name' => ['required', 'string', 'max:120'],
            'type' => ['sometimes', 'string', 'max:30'],
            'options' => ['sometimes', 'array'],
            'options.*' => ['string', 'max:120'],
            'unit' => ['sometimes', 'nullable', 'string', 'max:50'],
            'sortOrder' => ['sometimes', 'integer', 'min:0'],
        ];
    }
}
