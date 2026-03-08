<?php

namespace App\Modules\Spec\Requests;

use App\Http\Requests\ApiRequest;

class SetProductSpecsRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'specs' => ['required', 'array', 'min:1'],
            'specs.*.name' => ['required', 'string', 'max:120'],
            'specs.*.value' => ['required', 'string', 'max:255'],
            'specs.*.sortOrder' => ['sometimes', 'integer', 'min:0'],
        ];
    }
}
