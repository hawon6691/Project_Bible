<?php

namespace App\Modules\Spec\Requests;

use App\Http\Requests\ApiRequest;

class UpdateSpecDefinitionRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'name' => ['sometimes', 'string', 'max:120'],
            'type' => ['sometimes', 'string', 'max:30'],
            'options' => ['sometimes', 'array'],
            'options.*' => ['string', 'max:120'],
            'unit' => ['sometimes', 'nullable', 'string', 'max:50'],
            'sortOrder' => ['sometimes', 'integer', 'min:0'],
        ];
    }
}
